
# Software Requirements Specification (SRS)

## Project Overview

The Field Technician App is a Java Spring backend system for managing field technicians, customers, schedulers, admins, and job scheduling. It provides secure RESTful APIs for user registration, authentication, job creation, assignment, status tracking, and role-based access control. The system is designed for extensibility, security, and testability, with in-memory storage for development and demo purposes.

**Latest Change:**

- All Java packages have been renamed from `com.example.restservice` to `com.technican.restservice` throughout the codebase (main and test sources).
- All import and package statements have been updated accordingly.
- All tests pass and the project builds successfully after the refactor.

---

## 1. Functional Requirements


### 1.1 User Management

- **User Registration**
  - Register as a technician (with contact info), customer (with address), scheduler, or admin.
  - Only the first admin can be created without authentication; all other users require an authenticated admin.
  - Duplicate usernames are not allowed.
  - User registration requires `roles` as an array (e.g., `["ADMIN"]`).
  - If `roles` is missing or not an array, registration will fail or require admin authentication.
  - See README for payload examples and troubleshooting.
- **Authentication**
  - Users log in with username and password.
  - JWT tokens are issued for authenticated sessions.
  - All protected endpoints require a valid JWT.
- **Role-Based Access Control**
  - Only admins can create new users (except the first admin).
  - Role checks are enforced for job creation, assignment, and status updates.
  - All endpoints are protected by object-level authorization (IDOR protection): users can only access resources they own or are assigned to.

### 1.2 Job Management

- **Job Creation**
  - Authorized roles (admin, technician, scheduler) can create jobs for customers.
- **Job Assignment**
  - Admins and schedulers can assign technicians to jobs.
- **Job Status Update**
  - Admins, schedulers, and assigned technicians can update job status (`scheduled`, `in-progress`, `completed`).
- **Job Listing**
  - All jobs can be listed by authorized roles (admin, scheduler).
  - Technicians and customers can view only their assigned/requested jobs.
  - Technicians cannot view jobs not assigned to them (IDOR protection).
- **Job Deletion**
  - Admins and schedulers can delete jobs.
- **User Deletion**
  - Only admins can delete users (of any role).

### 1.3 API Endpoints

- `POST /users` — Register a new user
- `POST /login` — Authenticate and receive a JWT
- `POST /jobs` — Create a new job
- `GET /jobs` — List all jobs (admin, scheduler only)
- `GET /jobs/my` — Technician: view assigned jobs; Customer: view their jobs/requests
- `PUT /jobs/:id/status` — Update job status
- `POST /jobs/:id/assign` — Assign a technician to a job
- `DELETE /jobs/:id` — Delete a job (admin, scheduler only)
- `DELETE /users/:id` — Delete a user (admin only)

---

## 2. Non-Functional Requirements

### 2.1 Security

- **Input Validation**: All endpoints validate request bodies and parameters for type, length, and allowed values (using express-validator).
- **Rate Limiting**: Each IP is limited to 100 requests per 15 minutes (using express-rate-limit).
- **CORS**: Configurable; enabled for all origins by default (using cors).
- **Security Headers**: HTTP headers set for best practices (using helmet).
- **Password Hashing**: Passwords are hashed using bcryptjs.
- **JWT**: Tokens are signed and verified using a secret key from `.env`.
- **IDOR Protections**: Technicians and customers can only access their own jobs; only assigned technicians can update job status.

### 2.2 Testability

- **Unit and Integration Tests**: All endpoints and features are covered by Jest and Supertest tests.
- **Test Coverage**: Includes user registration, authentication, job management, role-based access, error handling, and security features. Tests are located in `tests/` and cover all endpoints, roles, and error cases.
- **TDD**: Test-driven development was used to ensure robust coverage and feature reliability.

### 2.3 Extensibility

- **In-Memory Storage**: Data is stored in memory for development; can be replaced with a database for production.

### 2.4 Performance

- **Efficient In-Memory Operations**: All operations are performed in memory for fast prototyping and testing.
- **Scalability**: Designed to be easily migrated to persistent storage and scalable infrastructure.

---

## 3. Data Model

### 3.1 User Types

- **Admin**: Can create users, assign jobs, and manage all data.
- **Technician**: Can view and update assigned jobs.
- **Customer**: Can view their own jobs/requests.
- **Scheduler**: Can create jobs and assign technicians.

### 3.2 Job

- **Fields**: id, customerId, serviceType, technicianId (optional), status, createdAt
- **Status**: `scheduled`, `in-progress`, `completed`

---

## 4. Error Handling

- All endpoints return appropriate HTTP status codes and error messages for invalid input, unauthorized access, missing fields, duplicate usernames, and not found resources.

---

## 5. Security & Best Practices

- All sensitive operations require authentication and proper role.
- JWT tokens are required for all protected endpoints.
- Input validation, rate limiting, CORS, and security headers are enforced globally.
- Passwords are never stored in plain text.
- IDOR protections are enforced for all user/job access.

---

## 6. Testing

- Tests are located in `tests/` and cover all endpoints, roles, and error cases.
- Run tests with manually if possible.
- All tests pass as of the latest implementation.
- 100% test coverage for utility functions.

---


### 7. Setup & Configuration

- **Java Version**: 24.0.1
- **Build System**: Gradle 8.14.2
- **Run (Windows/PowerShell)**:
  - `cd complete && gradlew.bat bootRun` (development server)
  - `cd complete && gradlew.bat test` (run all tests)
  - `java -jar build/libs/gs-rest-service-0.1.0.jar` (run packaged JAR)
- **Database**: Embedded H2 (in-memory, web console at `/h2-console`)
- **Configuration**: See `application.properties` for DB and JPA settings
- **Environment**: No external dependencies required for development/demo

**Note:**

- All Java source and test files now use the `com.technican.restservice` package. If you fork or extend this project, use the new package for all new code.

---

## 10. Troubleshooting

- **Admin Login 403 / No JWT Returned**:
  - Ensure you are using the correct admin username and password.
  - The first admin can be created without authentication; all others require an authenticated admin JWT.
  - After registering, login at `/login` with `{ "username": "admin", "password": "adminpass" }`.
  - If you receive a 403 or no JWT, check that the admin user exists in the USERS table and has the `ADMIN` role (not `ROLE_ADMIN`).
  - JWTs are required in the `Authorization: Bearer <token>` header for all protected endpoints.
  - If you manually edit the database, ensure roles are stored as `ADMIN` (uppercase, no prefix).
  - All logic is covered by automated tests; run `gradlew.bat test` to verify.

---

## 8. Future Enhancements

- **Persistent Storage**: Integrate with PostgreSQL, Redis, or other databases.
- **Production Security**: Restrict CORS origins, use environment-based secrets, and enable HTTPS.
- **User Management**: Add password reset, email verification, and audit logging.

---

## 9. References

- **Idempotency & Resilience**
  - All mutating endpoints (`POST`, `PUT`, `DELETE`) accept a client-generated `Idempotency-Key` header.
  - Identical requests with the same key return byte-for-byte identical responses.
  - Predictable, standardized error and success response shapes for all idempotent operations.

---

- All features are covered by automated unit tests (TDD, 100% endpoint coverage).
- In-memory repositories are used for development/testing; swap for persistent storage in production.
- Security best practices: password hashing, JWT, RBAC, IDOR protection, input validation.
- Legacy /greeting endpoint is deprecated and not part of the production API.
