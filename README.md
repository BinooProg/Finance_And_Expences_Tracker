# Finance and Expences Tracker

JavaFX desktop application for personal finance tracking with:
- User authentication
- Category management
- Transaction management
- Reports (income, expenses, balance, category summary)

## What the App Does
- Sign up and login with hashed passwords (MD5).
- Add, edit, and delete personal categories.
- Add and edit transactions (Income/Expense) per logged-in user.
- View total income, total expenses, balance, and per-category summaries.
- Use search/sort on categories, transactions, and reports.

## Tech Stack
- Java 21
- JavaFX
- Maven
- MySQL (JDBC)

## Prerequisites
- JDK 21 installed
- MySQL server running
- A MySQL user with permissions to create/use the app database and tables

## Database Setup (Required)
This app **requires MySQL**.

1. Open your MySQL client (MySQL Workbench / CLI).
2. Run the schema file:
   - `src/main/resources/schema.sql`
3. This will create/use:
   - `finance_and_expences_tracker`
   - `Users`, `Categories`, `Transactions` tables

## Configure Database Connection
Edit:
- `src/main/java/service/DatabaseConfig.java`

Set the values to match your MySQL server:
- `DEFAULT_URL` (host, port, database)
- `DEFAULT_USER`
- `DEFAULT_PASSWORD`

Current defaults:
- `jdbc:mysql://localhost:3306/finance_and_expences_tracker`
- `root`
- empty password

## Schema Notes
- `Users`: account/profile and password hash.
- `Categories`: per-user categories via `user_id`.
- `Transactions`: linked to `user_id` and `category_id`.
- Category name uniqueness is scoped per user.

## Run the App
From project root:

```bash
./mvnw javafx:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd javafx:run
```

## Default User Flow
1. Start app -> Login page.
2. Register a new account from Sign Up.
3. Login with that account.
4. Add categories and transactions.
5. Open Reports to view totals and grouped summary.

## Project Structure (Important Parts)
- `src/main/java/controller` - JavaFX UI controllers
- `src/main/java/service` - business logic and JDBC data access
- `src/main/java/model` - data models (`User`, `Category`, `Transaction`)
- `src/main/resources/fxml` - page layouts
- `src/main/resources/schema.sql` - database schema

## Troubleshooting
- **Unable to register / login**
  - Verify MySQL is running.
  - Verify credentials/URL in `DatabaseConfig`.
  - Ensure schema was executed from `schema.sql`.

- **SQL table/column errors**
  - Re-run `schema.sql` on database `finance_and_expences_tracker`.

- **`JAVA_HOME` not found**
  - Set `JAVA_HOME` to your JDK 21 installation path, then rerun Maven wrapper.
