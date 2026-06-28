 MONIFY — AI-Powered Money Manager Backend

Welcome to the backend of **MONIFY** (MoneyManager AI) — a modern, state-of-the-art personal finance tracker and intelligent advisor. Built with Java 21, Spring Boot 3.5.6, and Spring AI, MONIFY simplifies expense/income management, offers automated email summaries, and utilizes natural language processing (NLP) to parse and log transactions via LLMs (Groq Llama 3.3).

---

## 🌟 Key Features

### 1. 🔒 Security & Authentication
*   **JWT Authentication:** Fully stateless API security using JSON Web Tokens.
*   **OAuth2 Integration:** Native login support via **Google** and **GitHub** OAuth.
*   **Email Verification:** Registration activation workflow with secure tokens sent via SMTP.

### 2. 💸 Financial Tracking
*   **Income & Expense Logging:** Comprehensive management of transactions.
*   **Custom Categories:** Dynamically categorize transactions with custom categories per user.
*   **Multi-Criteria Filtering:** Flexible transaction searching and sorting by dates, keywords, amounts, and transaction types.

### 3. 🤖 AI-Driven Insights (Spring AI + Groq)
*   **Automated Insights:** Generates weekly/monthly personalized financial advice by analyzing transaction histories.
*   **NLP Quick Entry:** Add transactions by typing raw text (e.g. *"Spent 500 on groceries"* or *"Got 25000 salary"*). The AI parses and saves the transaction automatically.
*   **Structured Chat Assistant:** Interactive financial chat assistant with context of your current budget.
*   **Anomaly Detection:** Flags unusual spending changes comparing current month with last month.
*   **Rate Limiting:** Protects AI resources from abuse using a token-bucket rate limiter.

### 4. 📈 Reports & Data Export (Apache POI)
*   **Excel Export:** Download transaction details for any custom range in `.xlsx` format.
*   **Email Reports:** Direct-to-inbox Excel exports with beautifully designed rich-HTML templates.

### 5. ⏰ Automated Scheduled Jobs
*   **Daily Reminder:** Sends prompts to users to log their transactions.
*   **Daily Summary:** High-level summary of the day's expenses.
*   **Weekly & Monthly Summaries:** Detailed breakdown of savings and recommendations.

---

## 🛠️ Technology Stack

*   **Core Framework:** Spring Boot 3.5.6
*   **Language:** Java 21
*   **Database:** MySQL / PostgreSQL
*   **ORM:** Spring Data JPA (Hibernate)
*   **Security:** Spring Security, OAuth2 Client, JJWT (JSON Web Token)
*   **AI Engine:** Spring AI (OpenAI compatible wrapper configured for Groq Cloud API)
*   **File Export:** Apache POI OOXML
*   **Email:** Spring Mail (Brevo SMTP integration)
*   **Utility:** Project Lombok, Jakarta Validation

---

## 📂 Project Structure

```
Backend/
├── src/main/java/com/example/MONEYMANAGER/
│   ├── MoneymanagerApplication.java  # Main entry point
│   ├── config/                       # App Security, AI configurations
│   ├── constant/                     # Shared constants
│   ├── controller/                   # REST Endpoints
│   ├── dto/                          # Data Transfer Objects
│   ├── entity/                       # JPA Database Entities
│   ├── exception/                    # Global Exception Handling
│   ├── job/                          # Scheduled cron jobs (Daily, Weekly, Monthly)
│   ├── mapper/                       # Entity-to-DTO conversion layers
│   ├── middleware/                   # JWT filters, Rate limiters, OAuth success handler
│   ├── repository/                   # Database repository interfaces
│   ├── service/                      # Core business logic implementations
│   ├── util/                         # Date formats, token generators, helper methods
│   └── validator/                    # Request validators
└── src/main/resources/
    ├── application.properties        # Application configurations
    ├── static/                       # Static web resources
    └── templates/                    # HTML template structures
```

---

## ⚙️ Configuration & Environment Variables

MONIFY relies on the following environment variables. You can customize them in your environment or directly override them in `application.properties`:

| Variable Name | Description | Default Value |
| :--- | :--- | :--- |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `Kia@4165` |
| `JWT_SECRET` | Secure key for signing JWTs | *Pre-configured SHA256 string* |
| `SPRING_MAIL_HOST` | SMTP relay server for emails | `smtp-relay.brevo.com` |
| `SPRING_MAIL_PORT` | SMTP port | `587` |
| `SPRING_MAIL_USERNAME` | SMTP account username | *Brevo client user email* |
| `SPRING_MAIL_PASSWORD` | SMTP password/API Key | *Brevo API SMTP key* |
| `SPRING_MAIL_FROM` | Sender address for system emails | `deepakbhalerao195@gmail.com` |
| `GOOGLE_CLIENT_ID` | OAuth2 Google Client ID | *Pre-configured client credential* |
| `GOOGLE_CLIENT_SECRET`| OAuth2 Google Secret Key | *Pre-configured client credential* |
| `GITHUB_CLIENT_ID` | OAuth2 GitHub Client ID | *Pre-configured client credential* |
| `GITHUB_CLIENT_SECRET`| OAuth2 GitHub Secret Key | *Pre-configured client credential* |
| `GROQ_API_KEY` | Groq API Key for Spring AI Llama | *Pre-configured testing API Key* |

---

## 🚀 Setup & Getting Started

### Prerequisites
*   **Java Development Kit (JDK) 21** or higher installed.
*   **MySQL Server** running locally on port `3306` with a database named `moneymanagerdb`.
*   **Maven** installed (or use the included wrapper `./mvnw`).

### Installation
1.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd MoneymanagerAi/Backend
    ```

2.  **Verify Database Configuration:**
    Ensure MySQL is running, and create the schema:
    ```sql
    CREATE DATABASE moneymanagerdb;
    ```

3.  **Run the Application:**
    Using the Maven wrapper:
    ```bash
    ./mvnw spring-boot:run
    ```
    The server starts by default on `http://localhost:8080` with context path `/api/v1`.

4.  **Health Check:**
    Verify the application status:
    ```bash
    curl http://localhost:8080/api/v1/status
    # Expected response: "App is runnning"
    ```

---

## 🔌 API Endpoints Documentation

All requests should target the base path: `http://localhost:8080/api/v1`.

### 🔓 Public Endpoints (No Auth Needed)
*   `GET /status` - Quick server health check.
*   `POST /register` - User registration.
*   `GET /activate?token=<token>` - Activates registration (returns interactive HTML success/fail template).
*   `POST /login` - Standard email/password authentication (returns JWT and profile information).

### 🔐 Private Endpoints (Authorization Header Required)
Include the JWT token in headers: `Authorization: Bearer <your_jwt_token>`.

#### 🤖 AI Assistant (`/ai`)
*   `GET /ai/insights` - Returns full financial insights for the current month.
*   `POST /ai/quick-entry` - Logs dynamic natural text statements directly into the DB.
*   `POST /ai/chat` - Chat about finance with context-aware user transaction statistics.
*   `GET /ai/anomalies` - Detects spending anomalies comparing current month with last month.

#### 📊 Transaction & Category Management
*   `POST /expenses` - Add a new expense.
*   `GET /expenses` - Get current month expenses.
*   `GET /expenses/paged` - Paginated and sorted expense list.
*   `DELETE /expenses/{id}` - Delete an expense.
*   `POST /incomes` - Add a new income entry.
*   `GET /incomes` - Get current month incomes.
*   `GET /incomes/paged` - Paginated and sorted income list.
*   `DELETE /incomes/{id}` - Delete an income entry.
*   `POST /categories` - Create custom categories.
*   `GET /categories` - Get all user categories.
*   `GET /categories/{type}` - Get categories filtered by type (`income` or `expense`).

#### 🔍 Filters & Dashboard
*   `POST /filter` - Get filtered and sorted incomes/expenses based on keyword, range, type.
*   `GET /dashboard` - Fetch total aggregates and statistics overview.
*   `GET /dashboard/transactions/paged` - Paginated history list combining income & expenses.

#### 📧 Data Export & Email Reports
*   `GET /excel/download/incomes` - Direct Excel file download for incomes.
*   `GET /excel/download/expenses` - Direct Excel file download for expenses.
*   `GET /email/income-excel` - Generates and sends current month incomes spreadsheet to user's email.
*   `GET /email/expense-excel` - Generates and sends current month expenses spreadsheet to user's email.

---

## 📊 Database Schema Summary

MONIFY manages the following tables automatically via Hibernate:
*   `tbl_profile`: Stores user credentials, activation state, OAuth tokens, and metadata.
*   `tbl_category`: Store custom transaction tags.
*   `tbl_expense`: Logs debit/expense records linked to user profiles and categories.
*   `tbl_income`: Logs credit/income records linked to user profiles and categories.
