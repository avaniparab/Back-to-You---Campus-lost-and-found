# Back to You – Campus Lost & Found System

> *Lost something? Let's get it back to you.*

**Back to You** is a campus-focused Lost & Found Management System that enables students to report misplaced or recovered personal belongings, search and filter listings across campus landmarks, track personal submissions, and reconnect lost items with their owners. Administrators can monitor real-time platform statistics, manage student listings, and oversee user accounts through a protected administrative portal.

The application is engineered with a modern, decoupled full-stack architecture powered by **Java 21** and **Spring Boot 3.4.3**, cleanly separating the client-side presentation layer from the server-side API and persistence services.

---

## Technical Summary

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+) | Glassmorphism design system, responsive layouts, dynamic Fetch API client |
| **Backend** | Java 21, Spring Boot 3.4.3 | RESTful micro-architecture, Servlet-based Spring MVC controllers |
| **Web Layer** | Spring Web MVC | Dynamic resource routing and view forwarding (`WebConfig`) |
| **Persistence / ORM** | Spring Data JPA, Hibernate ORM | Relational entity mappings, automated schema validation |
| **Database** | MySQL (Database: `back_to_you`) | Relational persistence with foreign-key constraints and indexes |
| **Security & Auth** | Spring Security 6 | BCrypt password hashing, session-based authentication (`JSESSIONID`) |
| **Build Tool** | Maven Wrapper (`./mvnw`) | Dependency management and build lifecycle automation |
| **Development Server** | Embedded Apache Tomcat | Self-hosted Spring Boot server on port `8081` |
| **Deployment Target** | Apache Tomcat | Web Application Archive (WAR) package deployment |
| **Architecture** | Decoupled Client-Server | Separated `frontend/` and `backend/` directories |

---

## System Architecture

### Development Architecture

```text
Browser (Client)
   │
   │  HTTP / REST (Port 8081)
   ▼
Spring Boot Application
   ├── WebMvcConfigurer (WebConfig)
   │     └── Static Resource Handler ──► frontend/ (*.html, *.css, *.js)
   │
   └── Spring MVC REST Controllers (/api/**)
         │
         ▼
      Service Layer (AuthService, ItemService, AdminService)
         │
         ▼
      Spring Data JPA Repositories (UserRepository, ItemRepository)
         │  (Hibernate ORM)
         ▼
      MySQL Database (back_to_you)
```

---

## Project Structure

```text
Back-to-You/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/backtoyou/
│   │   │   │   ├── config/
│   │   │   │   │   └── WebConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AdminController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── ItemController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── AdminActionResponse.java
│   │   │   │   │   ├── AdminCreateUserRequest.java
│   │   │   │   │   ├── AdminStatsDto.java
│   │   │   │   │   ├── AdminStatsResponse.java
│   │   │   │   │   ├── AdminUpdateStatusRequest.java
│   │   │   │   │   ├── AdminUpdateUserRequest.java
│   │   │   │   │   ├── AdminUserDto.java
│   │   │   │   │   ├── AdminUsersResponse.java
│   │   │   │   │   ├── AuthResponse.java
│   │   │   │   │   ├── ItemCreateRequest.java
│   │   │   │   │   ├── ItemDto.java
│   │   │   │   │   ├── ItemResponse.java
│   │   │   │   │   ├── ItemUpdateRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── UserDto.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Item.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── ItemRepository.java
│   │   │   │   │   └── UserRepository.java
│   │   │   │   └── service/
│   │   │   │       ├── AdminService.java
│   │   │   │       ├── AuthService.java
│   │   │   │       └── ItemService.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/backtoyou/
│   │           ├── BackendApplicationTests.java
│   │           └── service/
│   │               └── AdminServiceTest.java
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── items.html
│   ├── item-details.html
│   ├── report-lost.html
│   ├── report-found.html
│   ├── my-reports.html
│   ├── admin-dashboard.html
│   ├── admin-users.html
│   ├── admin-items.html
│   ├── script.js
│   └── style.css
│
├── database/
│   └── back_to_you.sql
│
├── README.md
└── .gitignore
```

---

## Features

1. **User Registration & Login:** Self-service student registration with mandatory `@viva-technology.org` institutional email validation and secure authentication.
2. **Session-Based Authentication:** Standard HTTP session management (`JSESSIONID`) via Spring Security with automatic session invalidation on logout.
3. **Student & Admin Roles:** Server-enforced role separation (`STUDENT` vs. `ADMIN`) controlling access to application capabilities.
4. **Report Lost Item:** Structured submission form capturing item title, category, campus location, date lost, description, and client-side photo previews.
5. **Report Found Item:** Registration workflow for found items including designated campus custody handover checkpoints (e.g., Central Library, Lab Assistant, Security Gate).
6. **Browse Lost & Found Listings:** Dynamic, responsive catalog displaying active items with visual status badges and metadata.
7. **Search & Multi-Filter System:** Real-time client-side and server-side filtering by item type (`LOST` / `FOUND`), category, resolution status, and keywords.
8. **Item Details View:** Dedicated detail page (`item-details.html?id=X`) presenting item information, custody status, and reporter contact options.
9. **My Reports Dashboard:** Personalized dashboard allowing students to monitor the status of all their reported lost and found items.
10. **Update & Delete Personal Reports:** Students can edit report descriptions or remove active listings they created.
11. **Admin Dashboard:** Administrative portal displaying real-time metrics, breakdown counts, and platform-wide activity cards.
12. **Admin User Management:** Administrative user directory with account creation, detail updates, and role configuration.
13. **User Status Control:** Administrators can toggle accounts between `ACTIVE` and `INACTIVE` to suspend non-compliant users.
14. **Admin Item Management:** Administrative oversight to review, edit, or purge any reported item across campus.
15. **Mark Items Resolved:** Status transition from `ACTIVE` to `RESOLVED` when an item is safely returned to its rightful owner.
16. **Role-Based Access Control (RBAC):** Strict security filters barring students from accessing admin APIs (`HTTP 403 Forbidden`).
17. **Ownership-Based Mutation Control:** Item update and delete endpoints enforce that only the item author or an `ADMIN` can alter a listing.
18. **MySQL Persistence:** Relational database storage handled through JPA entity models (`User` and `Item`) with Hibernate validation.
19. **BCrypt Password Security:** Passwords hashed with BCrypt prior to database storage; plaintext passwords are never stored or logged.
20. **Validation & Error Handling:** Comprehensive field validation and uniform JSON error structures across all REST endpoints.

---

## Security & Business Rules

- **Public Access:** Unauthenticated users can freely view the home page, browse listings (`/api/items`), inspect item details (`/api/items/{id}`), and access login/registration pages.
- **Protected Actions:** Creating reports, viewing personal dashboards, updating listings, or accessing administrative features requires an active session.
- **Admin Isolation:** All `/api/admin/**` endpoints require `ROLE_ADMIN`. Any attempt by an unauthenticated user or student account yields `HTTP 403 Forbidden`.
- **Ownership Verification:** Before modifying or deleting an item (`PUT /api/items/{id}`, `DELETE /api/items/{id}`), the service verifies that the authenticated user is the original creator or has administrative privileges.
- **Self-Modification Restrictions:** Administrators cannot deactivate their own accounts or strip themselves of the `ADMIN` role.
- **Last Administrator Safeguard:** The application prohibits deleting, deactivating, or demoting the last active administrator account to prevent administrative lockout.
- **Referential Integrity on Deletion:** Users with associated lost or found reports cannot be deleted from the database; administrators must deactivate them instead.
- **Deactivated Account Enforcement:** Users marked as `INACTIVE` cannot authenticate; login requests for inactive accounts are rejected by Spring Security.

---

## REST API Reference

All API responses follow a uniform JSON structure: `{"success": true|false, ...}`.

### Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new student account (`name`, `email`, `password`) |
| `POST` | `/api/auth/login` | Public | Authenticate user credentials and establish session |
| `GET` | `/api/auth/session-check` | Public | Check if the current client session is authenticated |
| `POST` | `/api/auth/logout` | Authenticated | Terminate session and invalidate `JSESSIONID` |

### Item Endpoints (`/api/items`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/items` | Public | Retrieve items with optional query parameters (`type`, `category`, `status`, `search`, `mine`) |
| `GET` | `/api/items/{id}` | Public | Retrieve detailed information for a specific item |
| `POST` | `/api/items` | Authenticated | Submit a new lost or found item report |
| `GET` | `/api/items/my` | Authenticated | Retrieve all reports submitted by the authenticated user |
| `PUT` | `/api/items/{id}` | Owner / Admin | Update title, description, category, location, date, or status (`RESOLVED`) |
| `DELETE` | `/api/items/{id}` | Owner / Admin | Permanently delete an item report |

### Admin Endpoints (`/api/admin`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/users` | Admin Only | List all registered users (IDs, names, emails, roles, statuses) |
| `GET` | `/api/admin/stats` | Admin Only | Retrieve system-wide metrics (total users, active/inactive counts, items by type/status) |
| `POST` | `/api/admin/users` | Admin Only | Create a new user with specified role and status |
| `PUT` | `/api/admin/users/{id}` | Admin Only | Update an existing user's name, email, role, status, or password |
| `PATCH` | `/api/admin/users/{id}/status` | Admin Only | Toggle user account status between `ACTIVE` and `INACTIVE` |
| `DELETE` | `/api/admin/users/{id}` | Admin Only | Delete an unlinked user account |

---

## Database Architecture

The application connects to MySQL using the database: **`back_to_you`**.

The database schema reference and seed structure are maintained in:
```text
database/back_to_you.sql
```

### Table Definitions

1. **`users` Table**
   - `id`: INT AUTO_INCREMENT PRIMARY KEY
   - `name`: VARCHAR(255) NOT NULL
   - `email`: VARCHAR(255) NOT NULL UNIQUE
   - `password`: VARCHAR(255) NOT NULL (BCrypt hash)
   - `role`: ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT'
   - `status`: ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

2. **`items` Table**
   - `id`: INT AUTO_INCREMENT PRIMARY KEY
   - `user_id`: INT NOT NULL (Foreign Key referencing `users.id`)
   - `title`: VARCHAR(255) NOT NULL
   - `description`: TEXT NOT NULL
   - `category`: VARCHAR(100) NOT NULL
   - `location`: VARCHAR(255) NOT NULL
   - `date`: DATE NOT NULL
   - `type`: ENUM('LOST', 'FOUND') NOT NULL
   - `status`: ENUM('ACTIVE', 'RESOLVED') DEFAULT 'ACTIVE'
   - `image`: LONGTEXT (Optional base64 preview or image reference)
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## Local Development Setup

### Prerequisites

- **Java Development Kit (JDK):** Version 21
- **Database:** MySQL Server 5.7+ or 8.0+
- **Build Tool:** Maven (Maven Wrapper `./mvnw` is included in the project)
- **Version Control:** Git

> **macOS (Apple Silicon) JDK Path:**
> If installed via Homebrew, ensure your `JAVA_HOME` is pointed to:
> ```bash
> export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
> ```

### 1. Clone the Repository

```bash
git clone https://github.com/avaniparab/Back-to-You---Campus-lost-and-found.git
cd Back-to-You
```

### 2. Configure the MySQL Database

Log in to MySQL and create the database:
```sql
CREATE DATABASE back_to_you;
```

Import the database schema:
```bash
mysql -u root -p back_to_you < database/back_to_you.sql
```

Configure your local database credentials in [backend/src/main/resources/application.properties](file:///Users/avaniparab/workflow/Back-to-You/backend/src/main/resources/application.properties):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/back_to_you
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 3. Run Automated Tests

From the `backend` directory, run the test suite using the Maven Wrapper:
```bash
cd backend
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw test
```

### 4. Start the Application

Start the Spring Boot development server:
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw spring-boot:run
```

Once started, open your browser and navigate to:
```text
http://localhost:8081/
```

Spring Boot dynamically serves the frontend from `frontend/` on port `8081` while concurrently exposing the REST API at `http://localhost:8081/api/...`. No separate frontend server, Node.js process, or external web server is required.

---

## Deployment on Apache Tomcat

The application supports standard Java Web Application Archive (WAR) packaging for production deployment to an external **Apache Tomcat** Servlet container.

### Deployment Architecture

```text
Browser (Client)
   │
   │  HTTP / HTTPS
   ▼
Apache Tomcat (Servlet Container)
   │
   ▼
Spring Boot WAR (Back-to-You)
   ├── Spring MVC DispatcherServlet
   ├── WebConfig Static Resource Handlers
   └── Service & Security Layer
         │
         ▼
      MySQL Database (back_to_you)
```

### Packaging & Deployment Steps

1. **Build the WAR Package:**
   From the `backend` directory, compile and package the application:
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean package
   ```

2. **Locate the Build Artifact:**
   Maven outputs the compiled artifact inside:
   ```text
   backend/target/backend-0.0.1-SNAPSHOT.war
   ```
   *(or the configured archive name)*

3. **Deploy to Apache Tomcat:**
   Copy the generated `.war` file into your Tomcat installation's `webapps/` directory:
   ```bash
   cp backend/target/backend-0.0.1-SNAPSHOT.war /path/to/tomcat/webapps/back-to-you.war
   ```

4. **Start the Servlet Container:**
   Start Apache Tomcat:
   ```bash
   /path/to/tomcat/bin/startup.sh
   ```
   Tomcat automatically explodes and deploys the WAR archive. The application will be accessible via Tomcat's configured HTTP port (e.g., `http://localhost:8080/back-to-you/`).

---

## Automated Testing

Automated testing is implemented using **Spring Boot Test**, **JUnit 5**, and **Mockito**:

- **Context Verification (`BackendApplicationTests`):** Verifies that the Spring application context, JPA entity manager, security filter chains, and database connectivity load cleanly.
- **Service & Business Rule Tests (`AdminServiceTest`):** Comprehensive unit tests verifying administrative operations, role modifications, account status transitions, self-edit protections, and safeguards preventing modification of the sole remaining administrator.

### Current Test Suite Metrics

```text
Tests run: 16
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Run tests locally at any time via:
```bash
cd backend
./mvnw test
```

---

## User Interface & Design

The frontend implements a modern **Glassmorphism** aesthetic built with vanilla CSS tokens:
- **Translucent Surfaces:** Backdrops styled with blurred reflections and semi-transparent panels.
- **Harmonious Palette:** Slate blue, deep navy, and teal accents with high contrast for accessibility.
- **Responsive Navigation:** Adaptable hamburger menus, mobile drawer overlays, and responsive grid layouts.
- **Interactive Feedback:** Micro-animations on interactive cards, animated status badges, modal confirmations, and dynamic toast notifications.

---

## Academic Context & License

This project was developed as an academic web application to demonstrate full-stack engineering principles, including database schema design, RESTful API architecture, role-based access control, session management, and micro-service packaging.

- **Author:** Avani Parab
- **Institution:** Viva Institute of Technology
- **Academic Project:** Web Designing / Full-Stack Development
- **License:** Educational / Academic Use
