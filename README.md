Project

RESTful banking API with authentication, account management, transaction history and React frontend.
🏦 Bank Management System

Full-stack banking application with authentication, account management and transaction history.
🚀 Features

    User registration and login
    JWT authentication
    Role-based access (USER / ADMIN)
    Create bank accounts
    Deposit and withdraw money
    Transaction history
    Secure REST API
    Frontend UI for interacting with backend

🧰 Tech Stack
Backend

    Java
    Spring Boot
    Spring Security
    JWT
    PostgreSQL

Frontend

    React
    JavaScript
    CSS

DevOps

    Docker
    Docker Compose
    Git

🔐 Authentication

The application uses JWT (JSON Web Token) for authentication.

    Token is generated after login
    Token is required for protected endpoints
    Roles are embedded inside token

🗄️ Database

    PostgreSQL is used as main database
    Entities: User, Account, Transaction

🐳 Run with Docker

docker compose up --build
