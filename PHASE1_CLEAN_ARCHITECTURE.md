# Personal Finance and Expense Tracker (Phase 1) - Clean Simple Architecture

## 1) Goal
Build a JavaFX desktop app with 6 pages:
- Login
- Sign Up
- Dashboard
- Categories
- Transactions
- Reports

Use OOP classes, `.txt` file storage, MD5 password hashing, and JavaFX alerts for all validation errors.

## 2) Recommended Project Structure
```text
src/
  main/
    java/
      app/
        Launcher.java
        MainApp.java
      controller/
        LoginController.java
        SignupController.java
        DashboardController.java
        CategoriesController.java
        TransactionsController.java
        ReportsController.java
      model/
        User.java
        Category.java
        Transaction.java
      service/
        AuthService.java
        CategoryService.java
        TransactionService.java
        ReportService.java
      storage/
        FileRepository.java
        UserFileRepository.java
        CategoryFileRepository.java
        TransactionFileRepository.java
      util/
        HashUtil.java
        ValidationUtil.java
        AlertUtil.java
        IdGenerator.java
        Session.java
    resources/
      fxml/
        login.fxml
        signup.fxml
        dashboard.fxml
        categories.fxml
        transactions.fxml
        reports.fxml
      css/
        app.css
      data/
        users.txt
        categories.txt
        transactions.txt
```

## 3) Layer Responsibilities

### `model`
- Pure data classes only.
- No UI code and no file logic.

### `storage`
- Read/write `.txt` files.
- Convert lines <-> model objects.
- Keep one repository per entity.

### `service`
- Business rules and validation calls.
- Prevent duplicates.
- Ensure category exists before transaction save.
- Compute report totals.

### `controller`
- Handle button clicks and page inputs.
- Show JavaFX alerts.
- Call services only (not raw file access).

### `util`
- Shared helpers (MD5 hash, validation, alerts, session, ids).

## 4) Core Entities (Phase 1)

### `User`
- `id`
- `firstName`
- `lastName`
- `email`
- `passwordHash`

### `Category`
- `id`
- `name`

### `Transaction`
- `id`
- `userId`
- `categoryId`
- `amount`
- `type` (`Income` or `Expense`)
- `date`

## 5) File Format (Simple and Consistent)
Use pipe-separated values:

- `users.txt`
```text
1|Ali|Khan|ali@mail.com|5f4dcc3b5aa765d61d8327deb882cf99
```

- `categories.txt`
```text
1|Food
2|Salary
```

- `transactions.txt`
```text
1|1|2|2500.00|Income|2026-04-13
2|1|1|50.00|Expense|2026-04-14
```

## 6) Validation Rules (Where to Check)
- Empty fields -> controller + `ValidationUtil`, show alert.
- Email format -> `ValidationUtil`.
- Password and confirm password match -> `AuthService`.
- Duplicate email -> `AuthService`.
- Duplicate category -> `CategoryService`.
- Amount > 0 -> `TransactionService`.
- Type in (`Income`, `Expense`) -> `TransactionService`.
- Category exists -> `TransactionService`.
- Valid date selected -> `TransactionService`.

## 7) Navigation Flow
1. `login.fxml` -> successful login -> `dashboard.fxml`
2. `login.fxml` -> Sign Up button -> `signup.fxml`
3. `signup.fxml` -> successful registration -> `login.fxml`
4. `dashboard.fxml` -> Categories / Transactions / Reports pages
5. All inner pages include a Back/Home action to Dashboard

## 8) Minimal Service API (Suggested)

```java
// AuthService
boolean login(String email, String plainPassword);
void register(String first, String last, String email, String pass, String confirmPass);

// CategoryService
List<Category> getAll();
void addCategory(String name);
void updateCategory(int id, String name);

// TransactionService
List<Transaction> getByUser(int userId);
void addTransaction(int userId, int categoryId, double amount, String type, LocalDate date);
void updateTransaction(int id, int userId, int categoryId, double amount, String type, LocalDate date);

// ReportService
double totalIncome(int userId);
double totalExpense(int userId);
double balance(int userId); // income - expense
```

## 9) Implementation Notes
- Keep all alerts centralized in `AlertUtil` to avoid duplicate code.
- Store current logged-in user in `Session`.
- Do not access files from controllers directly.
- Keep FXML clean: each page has one dedicated controller.

This architecture is intentionally simple for Phase 1 and can be extended in Phase 2 by replacing file repositories with database repositories.
