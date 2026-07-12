# 🏦 Bank Management System

A full-stack banking application built with **Java Spring Boot** and **React** that demonstrates modern backend development practices including JWT authentication, role-based authorization, RESTful APIs, Dockerized deployment, and PostgreSQL persistence.

This project was created as a portfolio application for a **Junior Java Backend / Full-Stack Developer** position.

---

# ✨ Features

## Authentication
- User registration
- Secure login
- JWT authentication
- Password encryption with BCrypt
- Role-based authorization (USER / ADMIN)

## Account Management
- Create bank accounts
- View all user accounts
- Deposit money
- Withdraw money
- Balance validation

## Transactions
- Transaction history
- Deposit records
- Withdrawal records
- Automatic timestamp for every transaction

## Admin Panel
- View all registered users
- Role-based access using Spring Security

---

# 🛠 Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT Authentication
- PostgreSQL
- Maven

## Frontend

- React
- JavaScript
- CSS
- Axios

## DevOps

- Docker
- Docker Compose
- Git
- GitHub

---

# 🏗 Project Architecture

```
Frontend (React)
        │
        ▼
REST API (Spring Boot)
        │
        ▼
Spring Security
        │
        ▼
Business Layer
        │
        ▼
JPA / Hibernate
        │
        ▼
PostgreSQL
```

---

# 📂 Backend Structure

```
src
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── mapper
 ├── security
 ├── config
 ├── exception
 └── util
```

---

# 🔐 Security

The application uses Spring Security with JWT.

- Stateless authentication
- Protected endpoints
- Role-based authorization
- BCrypt password hashing
- JWT access token

---

# 🗄 Database

Main entities:

- User
- Account
- Transaction

Relationships:

- One User → Many Accounts
- One Account → Many Transactions

---

# 🐳 Run with Docker

```bash
docker compose up --build
```

Application will start:

Frontend

```
http://localhost:5174
```

Backend

```
http://localhost:8081
```

---

# 📡 REST API

Authentication

```
POST /api/auth/register
POST /api/auth/login
```

User

```
GET    /api/user/accounts
POST   /api/user/accounts
POST   /api/user/deposit
POST   /api/user/withdraw
GET    /api/user/accounts/{id}/transactions
```

Admin

```
GET /api/admin/users
```

---

# 🎯 Skills 

- Object-Oriented Programming
- REST API Development
- Spring Boot
- Spring Security
- JWT Authentication
- Hibernate / JPA
- PostgreSQL
- Docker
- React
- Git
- Layered Architecture
- DTO Pattern
- Exception Handling
- Role-Based Authorization

---

# 👨‍💻 Author

Developed as a portfolio project show Java Backend development skills.
