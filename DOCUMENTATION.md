# Ticket Management System --- Technical Documentation

## 1. System Overview

The Ticket Management System is a Spring Boot based support-ticket
application.

It has two primary roles:

-   **REQUESTOR**
-   **SUPPORT_ENGINEER**

A Requestor creates a ticket. A Support Engineer can work on assigned
tickets, add comments, change status, and perform
assignment/reassignment operations. Important actions are recorded in a
ticket history/audit trail.

------------------------------------------------------------------------

# 2. Complete Architecture

The application follows a layered backend architecture:

``` text
                         Client
                           |
                           v
                  HTML / CSS / JavaScript
                           |
                           | HTTP + JSON
                           v
              +---------------------------+
              | Spring Security Filter    |
              | Chain                     |
              |                           |
              | JwtAuthenticationFilter   |
              +-------------+-------------+
                            |
                            v
              +---------------------------+
              | Controller Layer          |
              +-------------+-------------+
                            |
                            v
              +---------------------------+
              | Service Layer             |
              |                           |
              | Business logic             |
              +-------------+-------------+
                            |
                            v
              +---------------------------+
              | Repository Layer          |
              +-------------+-------------+
                            |
                            v
              +---------------------------+
              | JPA / Hibernate           |
              +-------------+-------------+
                            |
                            v
              +---------------------------+
              | PostgreSQL / Neon         |
              +---------------------------+
```

Deployment architecture:

``` text
Developer
   |
   v
Git
   |
   v
GitHub
   |
   v
Render
   |
   v
Docker Container
   |
   v
Spring Boot Application
   |
   v
Neon PostgreSQL
```

------------------------------------------------------------------------

# 3. Project Structure

``` text
ticket-management/
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
├── .gitattributes
└── src/
    └── main/
        ├── java/
        │   └── com/sidhant/ticket_management/
        │       ├── controller/
        │       ├── service/
        │       ├── repository/
        │       ├── entity/
        │       ├── dto/
        │       │   ├── request/
        │       │   └── response/
        │       └── security/
        └── resources/
            ├── application.properties
            └── static/
                └── frontend/
                    ├── index.html
                    ├── script.js
                    └── style.css
```

------------------------------------------------------------------------

# 4. Layer Responsibilities

## 4.1 Controller Layer

The controller receives HTTP requests and returns HTTP responses.

Responsibilities:

-   Map URLs to Java methods
-   Receive request DTOs
-   Call services
-   Return response DTOs
-   Avoid putting core business logic directly in controllers

``` text
HTTP Request
     |
     v
Controller
     |
     v
Service
```

## 4.2 Service Layer

The service layer contains business logic.

Examples:

-   Validate requestor
-   Find banking client
-   Create ticket
-   Assign/reassign Support Engineer
-   Update ticket status
-   Add comments
-   Record ticket history
-   Validate Support Engineer role

``` text
Controller
     |
     v
Service
     |
     +--> Repository
     |
     +--> Business rules
     |
     +--> Audit history
```

## 4.3 Repository Layer

Repositories provide database access through Spring Data JPA.

``` text
Service
  |
  v
Repository
  |
  v
JPA / Hibernate
  |
  v
PostgreSQL
```

------------------------------------------------------------------------

# 5. Entity Architecture

The core persistent entities are:

``` text
User
BankingClient
Ticket
TicketComment
TicketHistory
```

## User

Important fields:

``` text
id
email
name
password
role
```

Roles:

``` text
REQUESTOR
SUPPORT_ENGINEER
```

## BankingClient

``` text
id
name
```

A ticket has a required BankingClient.

## Ticket

The implemented Ticket entity contains:

``` text
id
title
description
category
priority
status
attachment
requestor
bankingClient
supportEngineer
```

Relationships:

``` text
Ticket
  |
  +--> requestor --------> User
  |
  +--> bankingClient ----> BankingClient
  |
  +--> supportEngineer --> User (optional)
```

## TicketComment

Database structure:

``` text
id
comment
created_at
support_engineer_id
ticket_id
```

Relationships:

``` text
TicketComment
  |
  +--> ticket ------------> Ticket
  |
  +--> support_engineer --> User
```

## TicketHistory

Database structure:

``` text
id
action
created_at
details
performed_by
ticket_id
```

Relationships:

``` text
TicketHistory
  |
  +--> ticket -------> Ticket
  |
  +--> performed_by -> User
```

------------------------------------------------------------------------

# 6. Database Schema

``` text
+----------------------+
|        users         |
+----------------------+
| id PK                |
| email UNIQUE         |
| name                 |
| password             |
| role                 |
+----------+-----------+
           |
           | 1
           | 
           | *
+----------v-----------+
|       tickets        |
+----------------------+
| id PK                |
| title                |
| description          |
| category             |
| priority             |
| status               |
| attachment           |
| requestor_id FK      |
| banking_client_id FK |
| support_engineer_id  |
+------+-----------+---+
       |           |
       |           |
       |           +-------------------+
       |                               |
       | *                             | *
+------v-----------+          +---------v----------+
| ticket_comments  |          |   ticket_history   |
+------------------+          +---------------------+
| id PK            |          | id PK               |
| comment          |          | action               |
| created_at       |          | created_at           |
| support_engineer |          | details              |
| ticket_id FK     |          | performed_by FK      |
+------------------+          | ticket_id FK         |
                              +---------------------+

+----------------------+
|   banking_clients    |
+----------------------+
| id PK                |
| name UNIQUE          |
+----------------------+
```

Relationship summary:

``` text
User 1 -------- * Ticket
       requestor

User 1 -------- * Ticket
       supportEngineer

BankingClient 1 -------- * Ticket

Ticket 1 -------- * TicketComment

Ticket 1 -------- * TicketHistory
```

The Support Engineer relationship is optional because a newly created
ticket can initially be unassigned.

------------------------------------------------------------------------

# 7. Ticket Status Model

Implemented statuses:

``` text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

Typical lifecycle:

``` text
OPEN
  |
  v
IN_PROGRESS
  |
  v
RESOLVED
  |
  v
CLOSED
```

Status updates are also recorded in the audit trail.

------------------------------------------------------------------------

# 8. Authentication Architecture

Authentication uses Spring Security, JWT, and BCrypt.

``` text
Login Request
     |
     v
Auth Controller
     |
     v
Authentication Service
     |
     v
User Repository
     |
     v
PostgreSQL
     |
     v
BCrypt Password Verification
     |
     v
JWT Generation
     |
     v
Client
```

The password stored in the database is a BCrypt hash, not the plaintext
password.

------------------------------------------------------------------------

# 9. JWT Protected Request Flow

For a protected API request:

``` text
Client
  |
  | Authorization: Bearer <JWT>
  v
Spring Security Filter Chain
  |
  v
JwtAuthenticationFilter
  |
  +--> Extract JWT
  |
  +--> Validate JWT
  |
  +--> Identify user
  |
  +--> Load UserDetails
  |
  v
SecurityContext
  |
  v
Authorization
  |
  +--> Check required role
  |
  v
Controller
```

Authentication answers:

> Who is the user?

Authorization answers:

> Is this user allowed to perform this operation?

------------------------------------------------------------------------

# 10. Most Important: How a Request Travels

This is the complete request lifecycle.

## Step 1 --- Client

The frontend or Postman sends an HTTP request.

Example:

``` text
POST /api/support/tickets/{ticketId}/comments
```

The request contains:

``` text
HTTP method
URL
Authorization header
JSON body
```

------------------------------------------------------------------------

## Step 2 --- Tomcat / Spring Boot

The request reaches the embedded Tomcat server running inside Spring
Boot.

``` text
Internet
   |
   v
Render
   |
   v
Docker
   |
   v
Tomcat
   |
   v
Spring Boot
```

Locally, Tomcat runs on port 8080 by default. In Render, the application
uses the port supplied through the deployment environment.

------------------------------------------------------------------------

## Step 3 --- Spring Security Filter Chain

Before the request reaches the controller, Spring Security processes it.

``` text
HTTP Request
     |
     v
Security Filter Chain
```

The JWT authentication filter checks whether the request contains a
valid Bearer token.

``` text
Authorization: Bearer <JWT>
```

------------------------------------------------------------------------

## Step 4 --- JWT Authentication

The JWT filter:

1.  Extracts the token.
2.  Validates the token.
3.  Identifies the user.
4.  Loads user details.
5.  Places the authenticated user into Spring Security's
    SecurityContext.

Conceptually:

``` text
JWT
 |
 v
Validate
 |
 v
User identity
 |
 v
UserDetails
 |
 v
SecurityContext
```

------------------------------------------------------------------------

## Step 5 --- Authorization

After authentication, Spring Security checks the required role.

For example:

``` text
/api/support/**
```

requires:

``` text
SUPPORT_ENGINEER
```

If authorization fails:

``` text
HTTP 403 Forbidden
```

The controller is not executed.

------------------------------------------------------------------------

## Step 6 --- Controller

If authentication and authorization succeed:

``` text
Security
   |
   v
Controller
```

The controller receives the request data and calls the appropriate
service method.

The controller should remain thin.

------------------------------------------------------------------------

## Step 7 --- Service

The service performs the actual business logic.

For a comment:

``` text
TicketService
    |
    +--> Find ticket
    |
    +--> Identify Support Engineer
    |
    +--> Validate business rules
    |
    +--> Create comment
    |
    +--> Save comment
    |
    +--> Record audit event
```

------------------------------------------------------------------------

## Step 8 --- Repository

The service calls a repository.

``` text
Service
   |
   v
TicketCommentRepository
```

Spring Data JPA handles the repository implementation.

------------------------------------------------------------------------

## Step 9 --- Hibernate

Hibernate translates the entity operation into SQL.

For a comment insertion, the generated SQL is conceptually:

``` sql
INSERT INTO ticket_comments
    (comment, created_at, support_engineer_id, ticket_id)
VALUES
    (?, ?, ?, ?);
```

------------------------------------------------------------------------

## Step 10 --- PostgreSQL

PostgreSQL executes the SQL against the database.

``` text
Hibernate
   |
   v
JDBC
   |
   v
PostgreSQL / Neon
```

The result travels back:

``` text
PostgreSQL
   |
   v
JDBC
   |
   v
Hibernate
   |
   v
Repository
   |
   v
Service
```

------------------------------------------------------------------------

## Step 11 --- Response

The service returns the result to the controller.

The controller creates the HTTP response.

``` text
Database
   |
   v
Repository
   |
   v
Service
   |
   v
Controller
   |
   v
JSON Response
   |
   v
Frontend / Postman
```

Therefore the complete request path is:

``` text
CLIENT
  |
  v
HTTP REQUEST
  |
  v
TOMCAT
  |
  v
SPRING SECURITY FILTER CHAIN
  |
  +--> JWT AUTHENTICATION
  |
  +--> ROLE AUTHORIZATION
  |
  v
CONTROLLER
  |
  v
SERVICE
  |
  v
REPOSITORY
  |
  v
JPA / HIBERNATE
  |
  v
JDBC
  |
  v
POSTGRESQL / NEON
  |
  v
JDBC
  |
  v
HIBERNATE
  |
  v
REPOSITORY
  |
  v
SERVICE
  |
  v
CONTROLLER
  |
  v
HTTP RESPONSE
  |
  v
CLIENT
```

This is the most important architecture to understand when explaining
the project in an interview or review.

------------------------------------------------------------------------

# 11. Ticket Creation Request Flow

A Requestor creates a ticket.

Conceptually:

``` text
CreateTicketRequest
        |
        v
Security
        |
        v
Requestor authorization
        |
        v
Ticket Controller
        |
        v
TicketService.createTicket()
        |
        +--> Find Requestor
        |
        +--> Find BankingClient
        |
        +--> Create Ticket
        |
        +--> Set status OPEN
        |
        +--> Save Ticket
        |
        +--> Record TICKET_CREATED
        |
        v
TicketResponse
```

The initial ticket status is:

``` text
OPEN
```

The history entry is created after the ticket is saved.

------------------------------------------------------------------------

# 12. Assignment Request Flow

Ticket assignment:

``` text
Request
   |
   v
JWT Authentication
   |
   v
Authorization
   |
   v
Ticket Service
   |
   +--> Find Ticket
   |
   +--> Find Support Engineer
   |
   +--> Verify role = SUPPORT_ENGINEER
   |
   +--> Set supportEngineer
   |
   +--> Save Ticket
   |
   +--> Record TICKET_ASSIGNED
   |
   v
Response
```

The current system uses **manual assignment**. A Support Engineer ID is
supplied for assignment.

------------------------------------------------------------------------

# 13. Reassignment Request Flow

``` text
Request
   |
   v
Security
   |
   v
Controller
   |
   v
TicketService.reassignTicket()
   |
   +--> Find Ticket
   |
   +--> Find new Support Engineer
   |
   +--> Verify role
   |
   +--> Replace supportEngineer
   |
   +--> Save Ticket
   |
   +--> Record TICKET_REASSIGNED
   |
   v
Response
```

------------------------------------------------------------------------

# 14. Comment Request Flow

The frontend uses:

``` text
POST /api/support/tickets/{ticketId}/comments
```

with a JSON body containing the comment.

Flow:

``` text
Frontend
   |
   v
HTTP POST + JWT
   |
   v
JWT Filter
   |
   v
SUPPORT_ENGINEER authorization
   |
   v
Controller
   |
   v
Service
   |
   +--> Find ticket
   |
   +--> Identify engineer
   |
   +--> Create TicketComment
   |
   +--> Save
   |
   +--> Record history
   |
   v
Response
```

------------------------------------------------------------------------

# 15. Status Update Request Flow

The frontend sends a status update to the support API.

The current frontend implementation uses a POST request for this
operation.

``` text
Frontend
   |
   v
POST /api/support/tickets/{ticketId}/status
   |
   v
Security
   |
   v
Controller
   |
   v
Service
   |
   +--> Find ticket
   |
   +--> Store old status
   |
   +--> Set new status
   |
   +--> Save ticket
   |
   +--> Record STATUS_CHANGED
   |
   v
Response
```

------------------------------------------------------------------------

# 16. Ticket History / Audit Trail

The audit trail answers a different question from the Ticket table.

The Ticket table answers:

> What is the current state?

The History table answers:

> What happened to the ticket?

Example:

``` text
Ticket #10

TICKET_CREATED
       |
       v
TICKET_ASSIGNED
       |
       v
COMMENT_ADDED
       |
       v
STATUS_CHANGED
       |
       v
TICKET_REASSIGNED
       |
       v
STATUS_CHANGED
```

The frontend retrieves ticket history through:

``` text
GET /api/support/tickets/{ticketId}/history
```

The history response contains information such as:

``` text
action
details
performedBy
createdAt
```

------------------------------------------------------------------------

# 17. Frontend Architecture

The frontend is intentionally lightweight.

``` text
index.html
   |
   +--> UI structure
   |
   v
script.js
   |
   +--> API requests
   +--> JWT handling
   +--> UI updates
   +--> Ticket operations
   |
   v
style.css
   |
   +--> Presentation
```

A frontend action becomes an API request:

``` text
Button Click
    |
    v
JavaScript Function
    |
    v
API Request Helper
    |
    +--> JWT
    +--> HTTP method
    +--> JSON body
    |
    v
Spring Boot API
```

Examples implemented in the frontend include:

``` text
POST /api/support/tickets/{id}/status
POST /api/support/tickets/{id}/comments
POST /api/support/tickets/{id}/reassign
GET  /api/support/tickets/{id}/history
```

------------------------------------------------------------------------

# 18. DTO Flow

DTOs separate API contracts from persistence entities.

``` text
HTTP JSON
   |
   v
Request DTO
   |
   v
Service
   |
   v
Entity
   |
   v
Repository
```

Response:

``` text
Entity
   |
   v
Service
   |
   v
Response DTO
   |
   v
JSON
```

This avoids exposing persistence entities directly as the API contract.

------------------------------------------------------------------------

# 19. JPA / Hibernate Flow

When a repository operation occurs:

``` text
Repository
    |
    v
Spring Data JPA
    |
    v
Hibernate
    |
    v
SQL
    |
    v
PostgreSQL
```

Hibernate handles object-relational mapping between Java entities and
relational tables.

For example:

``` text
Ticket entity
     |
     v
tickets table

TicketComment entity
     |
     v
ticket_comments table

TicketHistory entity
     |
     v
ticket_history table
```

------------------------------------------------------------------------

# 20. Database Migration Architecture

The local PostgreSQL database was migrated to Neon.

``` text
Local PostgreSQL
       |
       | pg_dump
       v
PostgreSQL dump
       |
       | pg_restore
       v
Neon PostgreSQL
```

After migration, the deployed Spring Boot application uses Neon rather
than the local database.

------------------------------------------------------------------------

# 21. Environment Configuration

The production application does not store secrets in source control.

The application uses:

``` properties
spring.application.name=ticket-management

server.port=${PORT:8080}

spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=${JWT_SECRET}
```

Environment variables are supplied by the deployment platform.

This keeps:

``` text
Source Code
      !=
Production Secrets
```

------------------------------------------------------------------------

# 22. Docker Architecture

The application is packaged into a Docker image.

``` text
Dockerfile
    |
    v
Java 21 base image
    |
    v
Application source
    |
    v
Maven build
    |
    v
Spring Boot JAR
    |
    v
Container
```

The container runs the executable Spring Boot JAR.

------------------------------------------------------------------------

# 23. Render + Neon Production Architecture

``` text
                         INTERNET
                            |
                            v
                    Render Web Service
                            |
                            v
                       Docker
                            |
                            v
                    Spring Boot App
                     /                               /                                v               v
          Spring Security       REST API
                   |               |
                   +-------+-------+
                           |
                           v
                     JPA / Hibernate
                           |
                           v
                    Neon PostgreSQL
```

The frontend is served by the same Spring Boot application from the
static resources directory.

------------------------------------------------------------------------

# 24. Security Design

## Password Security

``` text
Plain Password
      |
      v
BCryptPasswordEncoder
      |
      v
Hash
      |
      v
Database
```

## JWT

After successful authentication:

``` text
User
 |
 v
Login
 |
 v
JWT
 |
 v
Client
 |
 v
Bearer Token
```

## Authorization

The system distinguishes between:

``` text
REQUESTOR
SUPPORT_ENGINEER
```

Authentication alone is not sufficient to access role-protected
operations.

------------------------------------------------------------------------

# 25. Error Scenarios

The service contains explicit failure conditions such as:

``` text
Requestor not found
Banking client not found
Ticket not found
Support Engineer not found
User is not a Support Engineer
```

The frontend catches API errors and displays an error message to the
user.

------------------------------------------------------------------------

# 26. Testing Strategy

A representative manual test is:

``` text
1. Login
      |
2. Create Ticket
      |
3. Verify ticket in database
      |
4. Assign Support Engineer
      |
5. Verify assignment
      |
6. Reassign
      |
7. Verify audit history
      |
8. Add Comment
      |
9. Verify comment
      |
10. Change Status
      |
11. Verify status
      |
12. View complete ticket history
```

The APIs were tested with Postman and the application was also tested
through the frontend.

------------------------------------------------------------------------

# 27. Important Design Decisions

## Why layered architecture?

It separates responsibilities:

``` text
Controller -> HTTP
Service    -> Business logic
Repository -> Persistence
```

This makes the application easier to maintain and test.

## Why DTOs?

They prevent API contracts from being tightly coupled to JPA entities.

## Why JPA/Hibernate?

They provide ORM and allow repositories to work with Java entities
rather than requiring handwritten SQL for standard operations.

## Why JWT?

It provides token-based authentication for API requests.

## Why BCrypt?

Passwords should not be stored in plaintext. BCrypt is designed for
password hashing.

## Why audit history?

The current ticket state cannot show how the ticket reached that state.
The history table preserves important actions over time.

## Why environment variables?

Database credentials and JWT secrets should not be committed to source
control.

## Why Docker?

Docker creates a repeatable runtime environment for deployment.

------------------------------------------------------------------------

# 28. Current Limitations

The current project focuses on the requested core ticket-management
workflow.

Possible future improvements:

-   Automatic ticket assignment based on workload
-   Pagination
-   Advanced search/filtering
-   Swagger/OpenAPI
-   Unit tests
-   Integration tests
-   Global exception handling
-   Optimistic locking for concurrent updates
-   Rate limiting
-   Structured production logging
-   Monitoring
-   Notifications
-   More granular permissions
-   External file storage for attachments

------------------------------------------------------------------------

# 29. Complete Mental Model

The most important flow to remember is:

``` text
                 REQUEST
                    |
                    v
            Spring Security
                    |
             +------+------+
             |             |
          JWT valid     JWT invalid
             |             |
             v             v
        Role check       401/403
             |
             v
          Controller
             |
             v
           Service
             |
      +------+------+
      |             |
 Business logic   Audit
      |             |
      +------+------+
             |
             v
         Repository
             |
             v
       JPA / Hibernate
             |
             v
         PostgreSQL
             |
             v
          Response
             |
             v
           Client
```

If you understand this request lifecycle, you understand the core
architecture of the application.
