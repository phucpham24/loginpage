# JWT Authentication with Spring Boot

## 📌 Overview
This project is a **Spring Boot** application that implements **JWT (JSON Web Token) authentication** for securing RESTful APIs. It provides a robust authentication and authorization mechanism using access tokens. Currently, **refresh token functionality is not yet implemented**.

## 🚀 Features
- 🔐 **User authentication** using JWT.
- 🔑 **Secure API access** with access tokens.
- 👤 **Role-based authorization** for different users.
- ✅ **Token validation middleware** to verify access tokens.
- ⚠️ **Exception handling** for authentication and authorization errors.

## 📌 API Endpoints

### 🔑 Authentication
- `POST /login` - Authenticate a user and return an access token.
- `POST /users` - Create a new user.

### 🔒 Protected Routes (Require Authentication)
- `GET /users` - Retrieve a list of users (requires authentication).
- `DELETE /users/{id}` - Delete a user by ID.
- `PUT /users` - Update user details.

## 🏗️ Project Structure
```
├── src/main/java/com/example/authentication
│   ├── config         # Security and JWT configurations
│   ├── controller     # REST API controllers
│   ├── model          # Entity classes
│   ├── repository     # Database repositories
│   ├── service        # Business logic & JWT handling
│   ├── exception      # Custom exception handling
├── src/main/resources
│   ├── application.properties  # App configuration
├── build.gradle (Gradle dependencies)
└── README.md
```


