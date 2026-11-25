package bank.banking;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Digital Banking Management System: Java OOP, SQLite, JDBC. */
public class DigitalBankingSystem {
    private static final Scanner IN = new Scanner(System.in);
    private static final String URL = System.getenv().getOrDefault("BANK_DB_URL", "jdbc:sqlite:digital_banking.db");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;

    public static void main(String[] args) {
        try (Connection db = DriverManager.getConnection(URL)) {
            initialize(db);
            bootstrapAdmin(db);
            System.out.println("\n====================================\n DIGITAL BANKING MANAGEMENT SYSTEM\n====================================");
            while (true) {
                System.out.println("\n1. Customer Registration\n2. Customer Login\n3. Admin Login\n4. Exit");
                switch (prompt("Enter your choice: ").trim()) {
                    case "1" -> registerCustomer(db);
                    case "2" -> customerLogin(db);
                    case "3" -> adminLogin(db);
                    case "4" -> { System.out.println("Goodbye."); return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            System.err.println("Ensure sqlite-jdbc.jar is beside the source and use run.bat.");
        }
    }

    private static void initialize(Connection db) throws SQLException {
        try (Statement s = db.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, full_name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('CUSTOMER','ADMIN')), created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), account_number TEXT NOT NULL UNIQUE, account_type TEXT NOT NULL CHECK(account_type IN ('SAVINGS','CURRENT')), balance REAL NOT NULL CHECK(balance >= 0), status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','FROZEN')), created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER NOT NULL REFERENCES accounts(id), transaction_type TEXT NOT NULL, amount REAL NOT NULL CHECK(amount > 0), balance_after REAL NOT NULL, note TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(account_id)");
        }
    }

    private static void bootstrapAdmin(Connection db) throws SQLException {
        try (PreparedStatement p = db.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN'"); ResultSet r = p.executeQuery()) {
            r.next(); if (r.getInt(1) > 0) return;
        }
        System.out.println("First run: create the initial administrator account.");
        String username = nonEmpty("Admin username: ");
        String name = nonEmpty("Admin full name: ");
        String email = nonEmpty("Admin email: ");
        String password = passwordPrompt("Admin password (minimum 8 characters): ");
        if (password.length() < 8) { System.out.println("Password must contain at least 8 characters. Restart to try again."); return; }
        try (PreparedStatement p = db.prepareStatement("INSERT INTO users(username,full_name,email,password_hash,role) VALUES(?,?,?,?, 'ADMIN')")) {
            p.setString(1, username); p.setString(2, name); p.setString(3, email); p.setString(4, hashPassword(password)); p.executeUpdate();
            System.out.println("Administrator account created.");
        } catch (SQLException e) { System.out.println("Could not create administrator: " + e.getMessage()); }
    }

    private static void registerCustomer(Connection db) {
        String username = nonEmpty("Username: ");
        String name = nonEmpty("Full name: ");
        String email = nonEmpty("Email: ");
        String password = passwordPrompt("Password (minimum 8 characters): ");
        if (password.length() < 8) { System.out.println("Password must contain at least 8 characters."); return; }
        try (PreparedStatement p = db.prepareStatement("INSERT INTO users(username,full_name,email,password_hash,role) VALUES(?,?,?,?, 'CUSTOMER')")) {
            p.setString(1, username); p.setString(2, name); p.setString(3, email); p.setString(4, hashPassword(password)); p.executeUpdate();
            System.out.println("Registration successful. You can now log in.");
        } catch (SQLException e) { System.out.println("Username or email already exists."); }
    }

    private static void customerLogin(Connection db) throws SQLException {
        User user = authenticate(db, "CUSTOMER");
        if (user == null) { System.out.println("Invalid username or password."); return; }
        System.out.println("Welcome, " + user.name + "!");
        while (true) {
            System.out.println("\nCUSTOMER MENU\n1. Open Bank Account\n2. View My Accounts\n3. Deposit Money\n4. Withdraw Money\n5. Transfer Money\n6. View Transaction History\n7. Calculate/Apply Interest\n8. Logout");
            String choice = prompt("Enter your choice: ").trim();
            switch (choice) {
                case "1" -> openAccount(db, user.id);
                case "2" -> listAccounts(db, user.id);
                case "3" -> transact(db, user.id, false);
                case "4" -> transact(db, user.id, true);
                case "5" -> transfer(db, user.id);
                case "6" -> transactionHistory(db, user.id);
                case "7" -> applyInterest(db, user.id);
                case "8" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void openAccount(Connection db, int userId) {
        System.out.println("Account type: 1. Savings (4% interest, minimum balance $500)  2. Current (no interest, minimum balance $1000)");
        String typeChoice = prompt("Choose account type: ").trim();
        String type = typeChoice.equals("1") ? "SAVINGS" : typeChoice.equals("2") ? "CURRENT" : null;
        if (type == null) { System.out.println("Invalid account type."); return; }
        String number = nonEmpty("Choose an account number: ");
        double initial = readAmount("Initial deposit: ");
        if (initial < 0 || (type.equals("SAVINGS") && initial < 500) || (type.equals("CURRENT") && initial < 1000)) {
            System.out.println("Initial deposit must be at least $500 for Savings or $1000 for Current."); return;
        }
        try {
            db.setAutoCommit(false);
            int accountId;
            try (PreparedStatement p = db.prepareStatement("INSERT INTO accounts(user_id,account_number,account_type,balance) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1, userId); p.setString(2, number); p.setString(3, type); p.setDouble(4, initial); p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Could not create account."); accountId = keys.getInt(1); }
            }
            addTransaction(db, accountId, "OPENING_DEPOSIT", initial, initial, "Initial deposit");
            db.commit(); System.out.println("Account created successfully. Account number: " + number);
        } catch (SQLException e) { rollback(db); System.out.println("Could not open account. Account number may already exist."); }
        finally { setAutoCommit(db, true); }
    }

    private static void listAccounts(Connection db, int userId) {
        String sql = "SELECT account_number,account_type,balance,status FROM accounts WHERE user_id=? ORDER BY id";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            p.setInt(1, userId); try (ResultSet r = p.executeQuery()) {
                System.out.println("\nACCOUNT DETAILS"); boolean found = false;
                while (r.next()) { found = true; System.out.printf("Account Number: %s | Type: %s | Balance: $%.2f | Status: %s%n", r.getString(1), titleCase(r.getString(2)), r.getDouble(3), r.getString(4)); }
                if (!found) System.out.println("No accounts found. Open an account first.");
            }
        } catch (SQLException e) { System.out.println("Unable to load accounts."); }
    }

    private static void transact(Connection db, int userId, boolean withdrawal) {
        String number = nonEmpty("Account number: "); double amount = readAmount(withdrawal ? "Withdrawal amount: " : "Deposit amount: ");
        if (amount <= 0) { System.out.println("Invalid transaction: amount must be positive."); return; }
        try {
            db.setAutoCommit(false);
            Account a = findOwnedAccount(db, userId, number);
            if (a == null || !a.status.equals("ACTIVE")) { rollback(db); System.out.println("Account not found or inactive."); return; }
            BankAccount account = createAccountModel(a);
            if (withdrawal) account.withdraw(amount); else account.deposit(amount);
            double newBalance = account.getBalance();
            if (Math.abs(newBalance - a.balance) < 0.0000001) {
                rollback(db);
                if (withdrawal) {
                    double minimum = a.type.equals("SAVINGS") ? 500.0 : 1000.0;
                    System.out.printf("Withdrawal denied. Minimum balance for %s account is $%.2f.%n", titleCase(a.type), minimum);
                } else System.out.println("Invalid deposit amount.");
                return;
            }
            try (PreparedStatement p = db.prepareStatement("UPDATE accounts SET balance=? WHERE id=?")) { p.setDouble(1, newBalance); p.setInt(2, a.id); p.executeUpdate(); }
            addTransaction(db, a.id, withdrawal ? "WITHDRAWAL" : "DEPOSIT", amount, newBalance, withdrawal ? "Cash withdrawal" : "Cash deposit");
            db.commit(); System.out.printf("%s successful. Updated balance: $%.2f%n", withdrawal ? "Withdrawal" : "Deposit", newBalance);
        } catch (SQLException e) { rollback(db); System.out.println("Transaction failed: " + e.getMessage()); }
        finally { setAutoCommit(db, true); }
    }

    private static void transfer(Connection db, int userId) {
        String from = nonEmpty("From account number: "); String to = nonEmpty("To account number: ");
        double amount = readAmount("Transfer amount: ");
        if (amount <= 0 || from.equals(to)) { System.out.println("Invalid transfer details."); return; }
        try {
            db.setAutoCommit(false);
            Account source = findOwnedAccount(db, userId, from); Account target = findAccount(db, to);
            if (source == null || target == null || !source.status.equals("ACTIVE") || !target.status.equals("ACTIVE")) { rollback(db); System.out.println("Source or destination account not found or inactive."); return; }
            double min = source.type.equals("SAVINGS") ? 500.0 : 1000.0;
            if (source.balance - amount < min) { rollback(db); System.out.printf("Transfer denied. Keep at least $%.2f in the source account.%n", min); return; }
            updateBalance(db, source.id, source.balance - amount); updateBalance(db, target.id, target.balance + amount);
            addTransaction(db, source.id, "TRANSFER_OUT", amount, source.balance - amount, "Transfer to " + to);
            addTransaction(db, target.id, "TRANSFER_IN", amount, target.balance + amount, "Transfer from " + from);
            db.commit(); System.out.println("Transfer successful.");
        } catch (SQLException e) { rollback(db); System.out.println("Transfer failed: " + e.getMessage()); }
        finally { setAutoCommit(db, true); }
    }

    private static void applyInterest(Connection db, int userId) {
        String number = nonEmpty("Account number: ");
        try {
            db.setAutoCommit(false); Account a = findOwnedAccount(db, userId, number);
            if (a == null || !a.status.equals("ACTIVE")) { rollback(db); System.out.println("Account not found or inactive."); return; }
            BankAccount account = createAccountModel(a);
            double interest = account.calculateInterest();
            if (interest <= 0) { rollback(db); System.out.println("Current accounts do not earn interest."); return; }
            double newBalance = a.balance + interest; updateBalance(db, a.id, newBalance);
            addTransaction(db, a.id, "INTEREST", interest, newBalance, "4% savings interest"); db.commit();
            System.out.printf("Interest added: $%.2f | Updated balance: $%.2f%n", interest, newBalance);
        } catch (SQLException e) { rollback(db); System.out.println("Unable to apply interest."); }
        finally { setAutoCommit(db, true); }
    }

    private static void transactionHistory(Connection db, int userId) {
        String number = nonEmpty("Account number: ");
        try {
            Account a = findOwnedAccount(db, userId, number);
            if (a == null) { System.out.println("Account not found."); return; }
            try (PreparedStatement p = db.prepareStatement("SELECT transaction_type,amount,balance_after,note,created_at FROM transactions WHERE account_id=? ORDER BY id DESC")) {
                p.setInt(1, a.id); try (ResultSet r = p.executeQuery()) {
                    System.out.println("\nTRANSACTION HISTORY"); boolean found = false;
                    while (r.next()) { found = true; System.out.printf("%s | $%.2f | Balance: $%.2f | %s | %s%n", r.getString(1), r.getDouble(2), r.getDouble(3), r.getString(4), r.getString(5)); }
                    if (!found) System.out.println("No transactions.");
                }
            }
        } catch (SQLException e) { System.out.println("Unable to load transaction history."); }
    }

    private static void adminLogin(Connection db) throws SQLException {
        User admin = authenticate(db, "ADMIN");
        if (admin == null) { System.out.println("Invalid administrator credentials."); return; }
        while (true) {
            System.out.println("\nADMIN MENU\n1. List All Accounts\n2. Search Account\n3. Freeze/Activate Account\n4. Banking Statistics\n5. List Customers\n6. Logout");
            switch (prompt("Enter your choice: ").trim()) {
                case "1" -> adminAccounts(db, null);
                case "2" -> adminAccounts(db, nonEmpty("Account number or customer name: "));
                case "3" -> changeAccountStatus(db);
                case "4" -> statistics(db);
                case "5" -> listCustomers(db);
                case "6" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void adminAccounts(Connection db, String search) {
        String sql = "SELECT a.account_number,u.full_name,a.account_type,a.balance,a.status FROM accounts a JOIN users u ON a.user_id=u.id";
        if (search != null) sql += " WHERE a.account_number LIKE ? OR u.full_name LIKE ?";
        sql += " ORDER BY a.id";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            if (search != null) { p.setString(1, "%" + search + "%"); p.setString(2, "%" + search + "%"); }
            try (ResultSet r = p.executeQuery()) {
                System.out.println("\nALL BANK ACCOUNTS"); boolean found = false;
                while (r.next()) { found = true; System.out.printf("%s | %s | %s | $%.2f | %s%n", r.getString(1), r.getString(2), titleCase(r.getString(3)), r.getDouble(4), r.getString(5)); }
                if (!found) System.out.println("No matching accounts.");
            }
        } catch (SQLException e) { System.out.println("Unable to list accounts."); }
    }

    private static void changeAccountStatus(Connection db) {
        String number = nonEmpty("Account number: ");
        try (PreparedStatement q = db.prepareStatement("SELECT status FROM accounts WHERE account_number=?")) {
            q.setString(1, number); try (ResultSet r = q.executeQuery()) {
                if (!r.next()) { System.out.println("Account not found."); return; }
                String newStatus = r.getString(1).equals("ACTIVE") ? "FROZEN" : "ACTIVE";
                try (PreparedStatement p = db.prepareStatement("UPDATE accounts SET status=? WHERE account_number=?")) { p.setString(1, newStatus); p.setString(2, number); p.executeUpdate(); }
                System.out.println("Account status changed to " + newStatus + ".");
            }
        } catch (SQLException e) { System.out.println("Unable to update account status."); }
    }

    private static void statistics(Connection db) {
        String sql = "SELECT COUNT(*), COALESCE(SUM(balance),0), SUM(CASE WHEN account_type='SAVINGS' THEN 1 ELSE 0 END), SUM(CASE WHEN account_type='CURRENT' THEN 1 ELSE 0 END) FROM accounts";
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); System.out.println("\nBANKING STATISTICS\nTotal accounts: " + r.getInt(1) + "\nTotal balance: $" + String.format(Locale.US, "%.2f", r.getDouble(2)) + "\nSavings accounts: " + r.getInt(3) + "\nCurrent accounts: " + r.getInt(4));
            try (ResultSet t = s.executeQuery("SELECT COUNT(*) FROM transactions")) { t.next(); System.out.println("Total transactions: " + t.getInt(1)); }
        } catch (SQLException e) { System.out.println("Unable to load statistics."); }
    }

    private static void listCustomers(Connection db) {
        try (Statement s = db.createStatement(); ResultSet r = s.executeQuery("SELECT username,full_name,email,created_at FROM users WHERE role='CUSTOMER' ORDER BY id")) {
            System.out.println("\nREGISTERED CUSTOMERS"); boolean found = false;
            while (r.next()) { found = true; System.out.printf("%s | %s | %s | %s%n", r.getString(1), r.getString(2), r.getString(3), r.getString(4)); }
            if (!found) System.out.println("No customers registered.");
        } catch (SQLException e) { System.out.println("Unable to list customers."); }
    }

    private static User authenticate(Connection db, String role) throws SQLException {
        String username = nonEmpty("Username: "); String password = passwordPrompt("Password: ");
        try (PreparedStatement p = db.prepareStatement("SELECT id,full_name,password_hash FROM users WHERE username=? AND role=?")) {
            p.setString(1, username); p.setString(2, role); try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !verifyPassword(password, r.getString(3))) return null;
                return new User(r.getInt(1), r.getString(2));
            }
        }
    }

    private static Account findOwnedAccount(Connection db, int userId, String number) throws SQLException {
        try (PreparedStatement p = db.prepareStatement("SELECT id,account_number,account_type,balance,status FROM accounts WHERE user_id=? AND account_number=?")) {
            p.setInt(1, userId); p.setString(2, number); try (ResultSet r = p.executeQuery()) { return r.next() ? new Account(r.getInt(1), r.getString(2), r.getString(3), r.getDouble(4), r.getString(5)) : null; }
        }
    }
    private static Account findAccount(Connection db, String number) throws SQLException {
        try (PreparedStatement p = db.prepareStatement("SELECT id,account_number,account_type,balance,status FROM accounts WHERE account_number=?")) {
            p.setString(1, number); try (ResultSet r = p.executeQuery()) { return r.next() ? new Account(r.getInt(1), r.getString(2), r.getString(3), r.getDouble(4), r.getString(5)) : null; }
        }
    }
    private static BankAccount createAccountModel(Account account) {
        return account.type.equals("SAVINGS")
                ? new SavingsAccount(account.number, account.balance)
                : new CurrentAccount(account.number, account.balance);
    }
    private static void updateBalance(Connection db, int id, double balance) throws SQLException {
        try (PreparedStatement p = db.prepareStatement("UPDATE accounts SET balance=? WHERE id=?")) { p.setDouble(1, balance); p.setInt(2, id); p.executeUpdate(); }
    }
    private static void addTransaction(Connection db, int accountId, String type, double amount, double balance, String note) throws SQLException {
        try (PreparedStatement p = db.prepareStatement("INSERT INTO transactions(account_id,transaction_type,amount,balance_after,note) VALUES(?,?,?,?,?)")) {
            p.setInt(1, accountId); p.setString(2, type); p.setDouble(3, amount); p.setDouble(4, balance); p.setString(5, note); p.executeUpdate();
        }
    }
    private static void rollback(Connection db) { try { db.rollback(); } catch (SQLException ignored) { } }
    private static void setAutoCommit(Connection db, boolean value) { try { db.setAutoCommit(value); } catch (SQLException ignored) { } }
    private static String prompt(String message) { System.out.print(message); return IN.nextLine(); }
    private static String nonEmpty(String message) { while (true) { String v = prompt(message).trim(); if (!v.isEmpty()) return v; System.out.println("This field cannot be empty."); } }
    private static double readAmount(String message) { while (true) { try { return Double.parseDouble(prompt(message).trim()); } catch (NumberFormatException e) { System.out.println("Enter a valid number."); } } }
    private static String passwordPrompt(String message) {
        Console console = System.console();
        if (console != null) { char[] chars = console.readPassword("%s", message); return new String(chars); }
        return prompt(message);
    }
    private static String hashPassword(String password) {
        byte[] salt = new byte[16]; RANDOM.nextBytes(salt); byte[] hash = derive(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    private static boolean verifyPassword(String password, String stored) {
        try { String[] parts = stored.split(":"); byte[] salt = Base64.getDecoder().decode(parts[0]); byte[] expected = Base64.getDecoder().decode(parts[1]); return java.security.MessageDigest.isEqual(expected, derive(password, salt)); }
        catch (RuntimeException e) { return false; }
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) { throw new IllegalStateException("Password hashing unavailable.", e); }
        finally { spec.clearPassword(); }
    }
    private static String titleCase(String s) { return s.substring(0, 1) + s.substring(1).toLowerCase(Locale.ROOT); }
    private record User(int id, String name) { }
    private record Account(int id, String number, String type, double balance, String status) { }

    // Abstraction: BankAccount defines common operations; SavingsAccount and CurrentAccount
    // provide account-specific interest and minimum-balance rules. Account data is encapsulated.
    private abstract static class BankAccount {
        protected final String accountNumber;
        protected double balance;
        protected BankAccount(String accountNumber, double balance) { this.accountNumber = accountNumber; this.balance = balance; }
        public abstract void withdraw(double amount);
        public abstract void deposit(double amount);
        public abstract double calculateInterest();
        public double getBalance() { return balance; }
        public String getAccountNumber() { return accountNumber; }
    }
    private static final class SavingsAccount extends BankAccount {
        private SavingsAccount(String number, double balance) { super(number, balance); }
        public void withdraw(double amount) { if (amount > 0 && balance - amount >= 500) balance -= amount; }
        public void deposit(double amount) { if (amount > 0) balance += amount; }
        public double calculateInterest() { return balance * 0.04; }
    }
    private static final class CurrentAccount extends BankAccount {
        private CurrentAccount(String number, double balance) { super(number, balance); }
        public void withdraw(double amount) { if (amount > 0 && balance - amount >= 1000) balance -= amount; }
        public void deposit(double amount) { if (amount > 0) balance += amount; }
        public double calculateInterest() { return 0; }
    }
}
