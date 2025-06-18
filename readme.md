# Appointments Microservice

This repository contains the backend application for appointment management. It provides the core APIs and business logic for creating and managing appointments and other related functionalities.

## Table of Contents
* [Features](#features)
* [Technologies Used](#technologies-used)
* [Project Structure](#project-structure)
* [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Cloning the Repository](#cloning-the-repository)
    * [Configuration](#configuration)
    * [Running the Application](#running-the-application)
* [API Endpoints](#api-endpoints)
* [Database](#database)
* [Authentication & Authorization](#authentication--authorization)
* [Testing](#testing)
* [Contributing](#contributing)
* [License](#license)

## Features

* **Appointment Management:** CRUD operations for appointments (add, view, update, delete).

## Technologies Used

* **Java:** Primary programming language.
* **Spring Boot:** Framework for building robust, production-ready, stand-alone Spring applications.
* **Spring Data JPA:** Simplifies data access layer development with JPA (Java Persistence API).
* **Hibernate:** JPA implementation for ORM (Object-Relational Mapping).
* **Maven:** Build automation tool for Java projects.
* **H2 Database (or other):** In-memory database for development/testing, or a persistent database like MySQL/PostgreSQL for production.
* **Lombok:** Reduces boilerplate code (getters, setters, constructors, etc.).
* **Spring Security:** For authentication and authorization.
* **JWT (JSON Web Tokens):** For stateless authentication.
* **RESTful APIs:** For communication with the frontend.

## Project Structure

The project follows a standard Spring Boot layered architecture:
```
src/main/java/com/mtbs/appointments
├── MtbsBackendApplication.java     # Main Spring Boot application class
├── controller                      # REST API endpoints
│   ├── AppointmentController.java  # Example controller
│   └── ...
├── service                         # Business logic layer
│   ├── AppointmentService.java     # Example service interface
│   ├── impl                        # Implementation of service interfaces
│   │   └── AppointmentServiceImpl.java
│   └── ...
├── repository                      # Data access layer (JPA repositories)
│   ├── AppointmentRepository.java  # Example repository interface
│   └── ...
├── model                           # JPA entities/Domain models
│   ├── Appointment.java            # Example entity
│   └── ...
├── dto                             # Data Transfer Objects (for request/response)
│   ├── AppointmentDTO.java         # Example DTO
│   └── ...
├── config                          # Spring configurations (e.g., SecurityConfig)
├── exception                       # Custom exception classes
└── util                            # Utility classes
└── security                        # Security-related classes (e.g., JWT filters)

src/main/resources
├── application.properties          # Application configuration (database, server port, etc.)
├── data.sql                        # SQL scripts for initial data (optional)
└── schema.sql                      # SQL scripts for database schema (optional)

src/test/java/com/mtbs/appointments    # Unit and Integration Tests
``` 


## Getting Started

Follow these instructions to set up and run the Appointment Management Backend on your local machine.

### Prerequisites

Before you begin, ensure you have the following installed:

* **Java Development Kit (JDK) 17 or higher**
* **Maven 3.6.0 or higher**
* **Git**
* **An IDE** (e.g., IntelliJ IDEA, Eclipse, VS Code with Java extensions)

### Cloning the Repository

```bash
git clone [https://github.com/kiranchintala/mtbs-backend.git](https://github.com/kiranchintala/mtbs-backend.git)
cd mtbs-backend

```
### Configuration

The primary configuration file is `src/main/resources/application.properties`.

* **Database Configuration:**
  By default, the project might be configured to use an H2 in-memory database for simplicity. You can change this to a persistent database like MySQL or PostgreSQL.

  **Example for H2 (in-memory):**
    ```properties
    spring.h2.console.enabled=true
    spring.datasource.url=jdbc:h2:mem:mtbsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=update # or create, none
    ```

  **Example for MySQL (adjust credentials and URL):**
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/mtbs_db?useSSL=false&serverTimezone=UTC
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.hibernate.ddl-auto=update # Use 'update' or 'create' for schema management
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
    ```
    * **`spring.jpa.hibernate.ddl-auto`**:
        * `none`: Do nothing with the schema.
        * `update`: Update the schema if necessary.
        * `create`: Creates the schema on startup, dropping existing tables. Use with caution for production.
        * `create-drop`: Creates on startup, drops on shutdown. Ideal for testing.
  * **Server Port:**
    The application typically runs on port 8080 by default. You can change it:
    ```properties
    server.port=8080
    ```

### Running the Application

1.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```

2.  **Run the Spring Boot application:**
    ```bash
    mvn spring-boot:run
    ```
    Alternatively, you can run the JAR file generated in the `target` directory:
    ```bash
    java -jar target/mtbs-backend-1.0-SNAPSHOT.jar # Adjust version if different
    ```
    The application will start, and you should see logs indicating it's running, typically on `http://localhost:8080`.

## API Endpoints

(You'll need to fill this section out with your actual endpoints. Here are examples.)

**Base URL:** `http://localhost:8080/api/v1` (or whatever your base path is)

### Authentication

* `POST /api/v1/auth/register` - Register a new user
* `POST /api/v1/auth/login` - Authenticate user and get JWT token

### Appointments

* `GET /api/v1/appointments` - Get list of appointments for an authenticated user
* `POST /api/v1/appointments` - Create a new appointment
* `GET /api/v1/appointments/{id}` - Get appointment details by ID
* `PUT /api/v1/appointments/{id}` - Update an appointment
* `DELETE /api/v1/appointments/{id}` - Delete an appointment

---

**You can use tools like Postman or Insomnia to test these endpoints.**

## Database

The project uses JPA with Hibernate for database interaction. You can configure it to use any relational database. For local development, H2 (in-memory) is often used for quick setup without needing a separate database server.

## Authentication & Authorization

This backend uses **Spring Security** with **JSON Web Tokens (JWT)** for authentication.

* Users register and log in to obtain a JWT.
* This token must be included in the `Authorization` header as a Bearer token for protected endpoints.
* Authorization is handled by roles (e.g., `ROLE_USER`, `ROLE_ADMIN`) which are configured within Spring Security.

## Testing

* **Unit Tests:** Located in `src/test/java/com/mtbs/appointments`.
* **Integration Tests:** You can implement integration tests to verify interactions between layers and with the database.

To run tests:

```bash
mvn test
```

## Contributing

Contributions are welcome! If you have suggestions or want to contribute:

1.  **Fork the repository.**
2.  **Create a new branch** (`git checkout -b feature/your-feature-name`).
3.  **Make your changes.**
4.  **Commit your changes** (`git commit -m 'Add new feature'`).
5.  **Push to the branch** (`git push origin feature/your-new-branch-name`).
6.  **Open a Pull Request.**

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details (if you have one, otherwise remove this section).