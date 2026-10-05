# Back to You – Campus Lost & Found System

> *Lost something? Let's get it back to you.*

Back to You is a campus-focused Lost & Found Management System that allows students to report lost or found items, browse reported items, search and filter listings, manage their own reports, and reconnect lost belongings with their owners. Administrators can manage users and reported items through a protected admin dashboard.

---

## Features

### Student Features
- **Student Registration:** Quick account creation with validation.
- **College Email Validation:** Enforces campus domain verification using `@viva-technology.org`.
- **Authentication:** Secure student login and session management/logout.
- **Report a Lost Item:** Submit lost item details including category, location, date, description, and optional photo preview.
- **Report a Found Item:** Register items found around campus to help reunite them with owners.
- **Browse Reported Items:** Dynamic catalog rendering lost and found items.
- **Search & Filter:** Filter listings by type (Lost/Found), category, location, or keyword search.
- **Item Details View:** Dedicated detail page for each item (`item-details.html?id=X`).
- **Contact Reporter:** Direct email action to contact the item reporter.
- **My Reports Dashboard:** View personal submissions with real-time status updates.
- **Mark Resolved:** Toggle item status from Active to Resolved once returned.
- **Delete Own Reports:** Remove personal reports from the system.
- **Responsive Interface:** Modern, mobile-friendly design across devices.

### Admin Features
- **Secure Admin Login:** Protected authentication for administrative accounts.
- **Role-Based Authorization:** Server-enforced permissions (`ADMIN` vs `STUDENT`).
- **Admin Dashboard:** Real-time system-wide statistics and metric cards.
- **System Statistics:** Dynamic counts for total users, total items, lost items, found items, and resolved cases.
- **Manage Users:** View registered user directory without exposing password hashes.
- **Manage Reported Items:** Admin overview of all campus listings.
- **Moderation Actions:** Ability to resolve or delete inappropriate/unnecessary listings.
- **Protected Admin Pages:** Access control denying unauthorized student access to admin endpoints (`HTTP 403 Forbidden`).

---

## How It Works

### Student Workflow
```
Register (@viva-technology.org) → Login → Report / Browse Items → View Details → Contact Reporter → Mark Resolved
```

### Admin Workflow
```
Login (Admin Credentials) → Protected Admin Dashboard → View Metrics → Manage Users & Items → Logout
```

---

## System Architecture

```text
Browser (Client)
   ↓
HTML5 + CSS3 + Vanilla JavaScript
   ↓  (JSON / HTTP Fetch API)
PHP Backend (8.5) + Sessions
   ↓  (PDO Prepared Statements)
MySQL Database (back_to_you)
```

- **Frontend (HTML5, CSS3, Vanilla JS):** Renders the user interface, handles form validations, communicates asynchronously with PHP backend endpoints, and dynamically updates the DOM without page reloads.
- **Backend (PHP 8.5):** Processes requests, validates input server-side, manages user session state (`$_SESSION`), enforces role-based access control, and interfaces with MySQL.
- **Database (MySQL):** Persistent storage layer maintaining relational tables for `users` and `items`.

---

## Project Structure

```text
Back-to-You/
│
├── database/
│   └── back_to_you.sql
│
├── php/
│   ├── add-item.php
│   ├── admin-get-users.php
│   ├── admin-stats.php
│   ├── db.php
│   ├── delete-item.php
│   ├── get-item.php
│   ├── get-items.php
│   ├── login.php
│   ├── logout.php
│   ├── register.php
│   ├── session-check.php
│   └── update-item.php
│
├── admin-dashboard.html
├── admin-items.html
├── admin-users.html
├── dashboard.html
├── index.html
├── item-details.html
├── items.html
├── login.html
├── my-reports.html
├── register.html
├── report-found.html
├── report-lost.html
├── script.js
└── style.css
```

### Purpose of Files & Folders
- `database/back_to_you.sql`: Database schema definition containing table structures, foreign keys, and indexes.
- `php/`: Server-side API endpoints for database connection, authentication, session management, item CRUD operations, and admin operations.
- `*.html`: Structure for student pages (Home, Browse, Details, Dashboards, Reporting forms) and Admin management portals.
- `script.js`: Client-side logic for DOM manipulation, form validation, dynamic fetch API requests, and event handling.
- `style.css`: Unified CSS design system defining layout tokens, modern styling, and responsive layout styling.

---

## Database

The application uses the MySQL database: **`back_to_you`**

### Tables & Schema Architecture

1. **`users` Table**
   - `id`: INT AUTO_INCREMENT PRIMARY KEY
   - `name`: VARCHAR(255)
   - `email`: VARCHAR(255) UNIQUE
   - `password`: VARCHAR(255) (Bcrypt hash)
   - `role`: ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT'
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

2. **`items` Table**
   - `id`: INT AUTO_INCREMENT PRIMARY KEY
   - `user_id`: INT (Foreign Key referencing `users.id`)
   - `title`: VARCHAR(255)
   - `description`: TEXT
   - `category`: VARCHAR(100)
   - `location`: VARCHAR(255)
   - `date`: DATE
   - `type`: ENUM('LOST', 'FOUND')
   - `image`: LONGTEXT / VARCHAR (Optional)
   - `status`: ENUM('ACTIVE', 'RESOLVED') DEFAULT 'ACTIVE'
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### Key Relationships & ENUM Definitions
- **Foreign Key:** `items.user_id → users.id`
- **User Roles:** `STUDENT` (default), `ADMIN` (elevated)
- **Item Types:** `LOST`, `FOUND`
- **Item Statuses:** `ACTIVE`, `RESOLVED`

---

## Requirements

- **Operating System:** macOS / Windows / Linux
- **Web Server:** Apache 2.4+
- **PHP Version:** PHP 8.x (Tested on PHP 8.5)
- **Database Server:** MySQL 5.7+ / 8.0+
- **Web Browser:** Modern browser (Chrome, Firefox, Safari, Edge)
- **Editor:** VS Code (Recommended)
- **Version Control:** Git (Optional)

---

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Back-to-You
```

### 2. Configure Apache Web Server
The project must be served through an Apache web server with PHP enabled. Do **not** open HTML files directly using `file://` or simple static HTTP servers (e.g. Python `http.server`).

Example URL format:
```text
http://localhost:8080/
```

### 3. Create & Import the MySQL Database
Log into your MySQL shell and create the database:
```sql
CREATE DATABASE back_to_you;
```
Import the schema from `database/back_to_you.sql`:
```bash
mysql -u root back_to_you < database/back_to_you.sql
```

### 4. Configure Database Connection Credentials
Open `php/db.php` and update the database connection variables if your local MySQL configuration differs:
```php
$host = '127.0.0.1';
$db   = 'back_to_you';
$user = 'root';
$pass = ''; // Enter your local MySQL password
```

### 5. Start Apache & MySQL Services
Commands vary depending on your operating system and service manager.

**For macOS (Homebrew):**
```bash
brew services start httpd
brew services start mysql
```

**For Windows (XAMPP / WampServer):**
Start Apache and MySQL modules through the Control Panel.

Navigate to `http://localhost:8080/` in your web browser.

### 6. Create an Admin Account
> **Note:** Public registration defaults strictly to `STUDENT` accounts for security.

To create an administrator account, insert an admin user record directly into MySQL with an encrypted password hash generated via PHP's `password_hash()`:

```sql
INSERT INTO users (name, email, password, role) 
VALUES ('System Administrator', 'admin@viva-technology.org', '<PASSWORD_HASH>', 'ADMIN');
```

---

## Authentication & Security

- **Password Hashing:** Passwords are securely hashed using PHP `password_hash()` with default strong algorithm (Bcrypt).
- **Password Verification:** Authentication uses `password_verify()` against stored hashes.
- **Session Management:** PHP sessions (`$_SESSION`) manage user authorization state.
- **Session Fixation Prevention:** Session IDs are regenerated upon successful login.
- **SQL Injection Prevention:** All SQL queries execute through PDO prepared statements with parameter binding.
- **Role-Based Access Control (RBAC):** Admin endpoints (`php/admin-stats.php`, `php/admin-get-users.php`) strictly enforce `$_SESSION['role'] === 'ADMIN'`.
- **Ownership Verification:** Item update and delete operations verify that `user_id` matches `$_SESSION['user_id']` or `$_SESSION['role'] === 'ADMIN'`.
- **API Payload Security:** Password hashes are excluded from all JSON API responses.

---

## API / PHP Endpoints

| Endpoint | Method | Purpose | Authentication |
| :--- | :--- | :--- | :--- |
| `php/register.php` | `POST` | Register a new student user | Public |
| `php/login.php` | `POST` | Authenticate user & start session | Public |
| `php/logout.php` | `GET` | Destroy current server session | Public / User |
| `php/session-check.php` | `GET` | Check logged-in user state & role | Public |
| `php/add-item.php` | `POST` | Submit a new LOST or FOUND report | Student / Admin |
| `php/get-items.php` | `GET` | Fetch items with filtering & search | Public |
| `php/get-item.php` | `GET` | Fetch specific item details by ID | Public |
| `php/update-item.php` | `POST` | Update item status (`RESOLVED`) | Owner / Admin |
| `php/delete-item.php` | `POST` | Delete an item report | Owner / Admin |
| `php/admin-get-users.php` | `GET` | Retrieve user directory | Admin Only |
| `php/admin-stats.php` | `GET` | Fetch overall database statistics | Admin Only |

---

## Running the Project

1. Start your local Apache web server.
2. Start your MySQL database server.
3. Verify `back_to_you` database is created and imported.
4. Open `http://localhost:8080/` in your browser.
5. Register a new student account using an `@viva-technology.org` email address.
6. Log in to access student reporting features.
7. Test submitting, browsing, filtering, and resolving reports.
8. Log in with an admin account to test the administrative dashboard and user management.

---

## Testing Checklist

### Student Verification
- [ ] Student Registration (`@viva-technology.org`)
- [ ] Student Login
- [ ] Student Logout
- [ ] Report Lost Item
- [ ] Report Found Item
- [ ] Browse Reported Items
- [ ] Keyword Search
- [ ] Category & Type Filters
- [ ] View Item Details Page
- [ ] View Personal Reports (`my-reports.html`)
- [ ] Mark Item as Resolved

### Admin Verification
- [ ] Admin Login
- [ ] Access Protected Admin Dashboard
- [ ] View System Statistics
- [ ] View User Directory (`admin-users.html`)
- [ ] View All Items Directory (`admin-items.html`)
- [ ] Perform Administrative Item Management
- [ ] Verify Student Access Blocked on Admin APIs (`HTTP 403`)
- [ ] Admin Logout

---

## Current Limitations

- **Image Previews:** Item image attachment currently uses client-side file previewing and is not permanently saved to server file storage.
- **Reporter Contacting:** Contacting a reporter triggers standard `mailto:` actions using the reporter's verified email.
- **Notifications:** No automated SMS or OTP email service.
- **Messaging:** Direct messaging between users is not currently built into the application.
- **Item Matching:** No automated AI matching algorithm between lost and found items.
- **Mapping:** No geolocation or Interactive Campus Map integration.

---

## Future Enhancements

- Server-side persistent file storage for item photos.
- Automated email notifications on item status updates.
- OTP verification during student registration.
- Automated AI matching between lost and found report attributes.
- Interactive campus map integration for drop-off and lost locations.
- In-app real-time messaging between item owner and finder.
- Dedicated mobile application version.

---

## Project Purpose

This project was developed as a college Web Designing mini-project to demonstrate practical full-stack application development principles, including HTML5 layout, CSS3 styling, Vanilla JavaScript DOM integration, PHP server-side scripting, PDO database interactions, MySQL query execution, session security, and role-based access control.

---

## License

This project was developed as an academic/educational project.

---

## Author

- **Developed by:** [Your Name]
- **College:** [Your College Name]
- **Academic Project:** Web Designing
