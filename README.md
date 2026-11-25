# 🏦 Digital Banking Management System

<p align="center">
  <strong>A Java console-based application to manage digital banking operations, customer accounts, and financial transactions.</strong>
  <br>
  Built with Java, SQLite, and JDBC.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk" alt="Java 17+">
  <img src="https://img.shields.io/badge/Database-SQLite-blue?logo=sqlite">
  <img src="https://img.shields.io/badge/Connectivity-JDBC-informational">
  <img src="https://img.shields.io/badge/Platform-Console-lightgrey">
  <img src="https://img.shields.io/badge/Project-Academic-purple">
</p>

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Banking Rules](#-banking-rules)
- [How It Works](#-how-it-works)
- [OOP Concepts](#-oop-concepts)
- [Database Structure](#-database-structure)
- [Project Structure](#-project-structure)
- [Requirements](#-requirements)
- [Installation and Setup](#️-installation-and-setup)
- [Run the Application](#️-run-the-application)
- [Security Features](#-security-features)
- [Troubleshooting](#️-troubleshooting)
- [Future Improvements](#-future-improvements)
- [Disclaimer](#-disclaimer)

---

## 📖 Overview

The **Digital Banking Management System** is a menu-driven Java console application developed using Object-Oriented Programming (OOP), SQLite, and JDBC.

The application provides basic digital banking operations for customers and administrators. Customers can register, log in, open savings or current accounts, deposit money, withdraw money, transfer funds, and view transaction history.

Administrators can manage customer accounts, search banking records, freeze or activate accounts, and view banking statistics.

The project uses an SQLite database for persistent storage, ensuring that customer information, account details, and transaction records remain available even after the application is closed.

It demonstrates Java programming, database connectivity, OOP principles, and basic banking transaction management.

## ✨ Features

| Feature | Description |
|---|---|
| 📝 Customer Registration | Register a new customer with username, full name, email, and password. |
| 🔐 Customer Login | Secure customer authentication. |
| 👤 Admin Login | Administrator authentication and management access. |
| 🏦 Open Bank Account | Create savings or current accounts. |
| 📋 View Accounts | Display customer account details and balances. |
| 💰 Deposit Money | Add money to an active bank account. |
| 💸 Withdraw Money | Withdraw money while maintaining the required minimum balance. |
| 🔄 Fund Transfer | Transfer money between bank accounts. |
| 📜 Transaction History | View previous deposits, withdrawals, transfers, and interest transactions. |
| 📈 Interest Calculation | Calculate and apply savings account interest. |
| 🔎 Search Accounts | Administrators can search accounts using account numbers or customer names. |
| ❄️ Freeze Accounts | Administrators can freeze or activate bank accounts. |
| 📊 Banking Statistics | View account and transaction statistics. |
| 💾 Persistent Storage | Store banking information using SQLite. |

### 👤 Customer Operations

- Customer registration and login.
- Open savings accounts.
- Open current accounts.
- View personal bank accounts.
- Deposit money.
- Withdraw money.
- Transfer funds between accounts.
- View transaction history.
- Calculate and apply savings interest.
- Logout securely.

### 🔐 Administrator Operations

- Initial administrator account setup.
- Administrator login.
- List all bank accounts.
- Search accounts.
- Freeze bank accounts.
- Activate frozen accounts.
- View customer information.
- View banking statistics.
- Logout.

## 💳 Banking Rules

The application implements different rules for savings and current accounts.

| Account Type | Interest Rate | Minimum Balance |
|---|---:|---:|
| Savings Account | 4% | $500 |
| Current Account | 0% | $1000 |

**Savings Account:**
- Earns 4% interest.
- Must maintain a minimum balance of $500.
- Supports deposits and withdrawals.

**Current Account:**
- Does not earn interest.
- Must maintain a minimum balance of $1000.
- Supports deposits and withdrawals.

Note: The application uses simulated balances and displays amounts in dollars.

## 🔄 How It Works

1. The application establishes a database connection using JDBC.
2. SQLite initializes the required database tables automatically.
3. On the first run, the administrator creates an initial admin account.
4. Customers can register and log in.
5. Users select the required banking operation from the console menu.
6. Java processes the selected operation.
7. JDBC executes the corresponding SQL statements.
8. SQLite stores or retrieves the required information.
9. The result is displayed in the console.

## 🧠 OOP Concepts

The project demonstrates the following Object-Oriented Programming concepts:

**1. Encapsulation**

- Uses private fields to protect account and customer information.
- Provides controlled access through methods.
- Maintains data integrity.

**2. Abstraction**

- Uses an abstract `BankAccount` class.
- Defines common banking operations.
- Hides implementation details from the user.

**3. Inheritance**

- `SavingsAccount` extends `BankAccount`.
- `CurrentAccount` extends `BankAccount`.
- Reuses common account properties and methods.

**4. Polymorphism**

- Implements account-specific withdrawal operations.
- Provides different interest calculation implementations.
- Uses method overriding to achieve runtime polymorphism.

**5. Classes and Objects**

The application uses classes such as:

- `DigitalBankingSystem`
- `BankAccount`
- `SavingsAccount`
- `CurrentAccount`
- Customer and account-related data models.

The application is maintained in a single Java source file using nested class definitions.

## 🗄️ Database Structure

The application uses **SQLite** as its database and JDBC for database connectivity.

The database file `digital_banking.db` is created automatically when the application runs.

### Database Tables

**1. Users Table**

Stores customer and administrator information.

| Column | Description |
|---|---|
| id | Unique user ID |
| username | User login name |
| full_name | Full name |
| email | Email address |
| password_hash | Hashed password |
| role | CUSTOMER or ADMIN |
| created_at | Account creation time |

**2. Accounts Table**

Stores bank account information.

| Column | Description |
|---|---|
| id | Unique account ID |
| user_id | Associated user ID |
| account_number | Unique bank account number |
| account_type | SAVINGS or CURRENT |
| balance | Current account balance |
| status | ACTIVE or FROZEN |
| created_at | Account creation time |

**3. Transactions Table**

Stores banking transaction details.

| Column | Description |
|---|---|
| id | Unique transaction ID |
| account_id | Associated account ID |
| transaction_type | Type of transaction |
| amount | Transaction amount |
| balance_after | Balance after transaction |
| note | Transaction description |
| created_at | Transaction timestamp |

The database schema is available in `schema.sql`.

## 📁 Project Structure

```text
DigitalBankingManagementSystem/
│
├── DigitalBankingSystem.java
│
├── schema.sql
│
├── README.md
│
├── run.bat
│
├── sqlite-jdbc.jar
│
├── .gitignore
│
└── digital_banking.db
    (Generated automatically when running)
```

**File Description**

| File | Purpose |
|---|---|
| DigitalBankingSystem.java | Main Java application containing banking operations and OOP classes. |
| schema.sql | SQL database schema. |
| README.md | Project documentation. |
| run.bat | Windows script to compile and launch the application. |
| sqlite-jdbc.jar | SQLite JDBC database driver. |
| .gitignore | Specifies files excluded from Git tracking. |
| digital_banking.db | Automatically generated SQLite database. |

## 🧰 Requirements

Make sure you have the following:

- Java Development Kit (JDK) 17 or newer.
- Windows operating system for using `run.bat`.
- SQLite JDBC driver (`sqlite-jdbc.jar`).
- Command Prompt (CMD).

**Note:** No separate SQLite installation is required. The application creates the database automatically.

## ⚙️ Installation and Setup

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/DigitalBankingManagementSystem.git
```

Navigate to the project folder:

```bash
cd DigitalBankingManagementSystem
```

Replace `YOUR_USERNAME` with your GitHub username.

### Step 2 — Verify Java Installation

Open Command Prompt and execute:

```bash
java -version
```

Check the Java compiler:

```bash
javac -version
```

Make sure JDK 17 or newer is installed.

### Step 3 — Verify JDBC Driver

Ensure that `sqlite-jdbc.jar` is available in the project folder.

The JDBC driver allows Java to communicate with the SQLite database.

### Step 4 — Database Initialization

No manual database creation is required.

When the application starts:

- SQLite establishes the database connection.
- The required tables are created automatically.
- Foreign key constraints are enabled.
- The database is stored as `digital_banking.db` in the current folder.

The `schema.sql` file is also provided for reference.

## ▶️ Run the Application

### Step 1 — Run on Windows

Open the extracted project folder.

Double-click:

`run.bat`

The script automatically:

1. Checks whether Java JDK is installed.
2. Checks whether the SQLite JDBC driver exists.
3. Compiles the Java source file.
4. Launches the banking application.

### Step 2 — Run Using Command Prompt

Open CMD inside the project folder.

Compile the application:

```bash
javac -d . DigitalBankingSystem.java
```

Run the application:

```bash
java --enable-native-access=ALL-UNNAMED -cp ".;sqlite-jdbc.jar" bank.banking.DigitalBankingSystem
```

### Step 3 — First-Time Administrator Setup

On the first run, the application prompts you to create an administrator account.

Enter:

- Administrator username.
- Full name.
- Email address.
- Password (minimum 8 characters).

After successful registration, the administrator can log in and manage banking operations.

### Step 4 — Customer Registration

Customers can select the registration option from the main menu.

After registration, they can log in and access banking services.

## 🔐 Security Features

The project implements basic security practices:

**Password Hashing**
- Passwords are not stored as plain text.
- Uses PBKDF2-HMAC-SHA256 for password hashing.
- Uses randomly generated salt values.
- Uses 120,000 iterations and a 256-bit derived key.

**Database Security**
- Uses JDBC prepared statements for database operations.
- Uses database transactions for important banking operations.
- Enables SQLite foreign key constraints.
- Maintains relationships between users, accounts, and transactions.

**Account Management**
- Supports account freezing and activation.
- Restricts transactions on inactive accounts.
- Validates minimum account balances.

## 🛠️ Troubleshooting

<details>
<summary><strong>Java is not recognized</strong></summary>

Install JDK 17 or newer and configure the Java `bin` directory in your system PATH.

Reopen Command Prompt after updating the environment variables.

</details>

<details>
<summary><strong>SQLite JDBC driver not found</strong></summary>

Check whether `sqlite-jdbc.jar` exists in the project folder.

Ensure that the filename matches the classpath command.

</details>

<details>
<summary><strong>Compilation failed</strong></summary>

Check your Java installation and ensure that you are running the command from the correct project folder.

Use:

```bash
javac -d . DigitalBankingSystem.java
```

</details>

<details>
<summary><strong>Database connection error</strong></summary>

Ensure that the SQLite JDBC driver is available.

Check that the application has permission to create or access the database file.

</details>

<details>
<summary><strong>Administrator account already exists</strong></summary>

The initial administrator setup is performed only when no administrator account exists in the database.

Use the existing administrator login.

</details>

<details>
<summary><strong>Application does not start using run.bat</strong></summary>

Ensure that:

- JDK 17 or newer is installed.
- `sqlite-jdbc.jar` exists in the same folder.
- `DigitalBankingSystem.java` is present.
- The project folder is extracted completely.

</details>

## 🚀 Future Improvements

The project can be enhanced with the following features:

- Add a graphical user interface (GUI).
- Develop a web-based banking dashboard.
- Add OTP-based authentication.
- Add email notifications for transactions.
- Generate downloadable transaction statements.
- Add advanced transaction filtering.
- Implement additional banking account types.
- Add automated unit and integration testing.
- Improve administrator activity monitoring.

