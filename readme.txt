# JWT Authentication with Spring Boot

## Overview
This project is a Spring Boot application that implements JSON Web Token (JWT) authentication and access tokens. It provides secure authentication and authorization mechanisms for RESTful APIs. Currently, the refresh token functionality has not yet been implemented.

## Features
- User authentication using JWT.
- Secure access to protected resources via access tokens.
- Role-based authorization.
- Token validation middleware.
- Exception handling for authentication and authorization errors.

## API Endpoints
### Authentication
- `POST /login` - Authenticate a user and return an access token.
- `POST /users` - Create a user.
### Protected Routes (Require Authentication)
- `GET /users` - Get authenticated list users.
- `DELETE /users/id` - Delete a user.
- `PUT /users` - Update a user.

## Security Implementation
- Uses **Spring Security** for authentication and authorization.
- Implements JWT-based authentication with token expiration.
- Secured API endpoints using role-based access control (RBAC).