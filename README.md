# Ticket Management System

A full-stack support ticket management system built with **Java 21,
Spring Boot, Spring Security, JWT, Spring Data JPA/Hibernate,
PostgreSQL, and HTML/CSS/JavaScript**.

The system supports two primary roles:

-   **Requestor** --- creates and tracks support tickets.
-   **Support Engineer** --- works on tickets, assigns/reassigns
    tickets, adds comments, changes status, and views ticket history.

The project was developed as a backend-focused mini project, documented,
containerized with Docker, connected to Neon PostgreSQL, and deployed on
Render.

## Live Application

https://ticket-management-lyou.onrender.com/frontend/index.html

## Features

-   JWT-based authentication
-   BCrypt password hashing
-   Role-based authorization
-   Requestor and Support Engineer roles
-   Ticket creation
-   Ticket assignment and reassignment
-   Ticket status updates
-   Ticket comments
-   Ticket history / audit trail
-   Banking-client association
-   PostgreSQL persistence
-   JPA/Hibernate ORM
-   HTML/CSS/JavaScript frontend
-   Environment-variable based secrets
-   Docker deployment
-   Neon PostgreSQL migration
-   Render deployment

## Technology Stack

  Area               Technology
  ------------------ -----------------------
  Language           Java 21
  Backend            Spring Boot
  Security           Spring Security + JWT
  Password hashing   BCrypt
  Persistence        Spring Data JPA
  ORM                Hibernate
  Database           PostgreSQL
  Hosted DB          Neon
  Frontend           HTML, CSS, JavaScript
  Containerization   Docker
  Deployment         Render
  Version control    Git / GitHub
  API testing        Postman

## Architecture

``` text
Frontend
   |
   v
Spring Security Filter Chain
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
JPA / Hibernate
   |
   v
PostgreSQL / Neon
```

Deployment:

``` text
GitHub -> Render -> Docker -> Spring Boot -> Neon PostgreSQL
```

## Database

Core tables:

``` text
users
banking_clients
tickets
ticket_comments
ticket_history
```

A Ticket belongs to a Requestor and BankingClient and may have a Support
Engineer assigned to it. Tickets can have many comments and many history
entries.

## Ticket Lifecycle

``` text
OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
```

Assignment/reassignment and important actions are recorded in
`ticket_history`.

## Audit Events

Examples:

``` text
TICKET_CREATED
TICKET_ASSIGNED
TICKET_REASSIGNED
STATUS_CHANGED
COMMENT_ADDED
```

## Security Flow

``` text
Login
  |
  v
Credentials verified
  |
  v
JWT generated
  |
  v
Client sends Bearer JWT
  |
  v
JwtAuthenticationFilter
  |
  v
User authenticated
  |
  v
Role authorization
  |
  v
Controller
```

## Configuration

Production secrets are not committed to GitHub.

``` properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
jwt.secret=${JWT_SECRET}
```

## Run Locally

``` bash
git clone <repository-url>
cd ticket-management
./mvnw spring-boot:run
```

Set the required environment variables first:

``` text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
```

Default local port:

``` text
http://localhost:8080
```

## Testing Flow

``` text
Login
  -> Create Ticket
  -> Assign
  -> Reassign
  -> Add Comment
  -> Change Status
  -> View Ticket History
```

## Future Improvements

-   Global exception handling
-   Bean validation
-   Pagination/filtering
-   Swagger/OpenAPI
-   Automated tests
-   Optimistic locking
-   Rate limiting
-   Better observability
-   Automatic ticket assignment
-   More granular permissions

## Author

**Sidhant Mahajan**
