# Fitness App — Microservices Backend

A Spring Boot microservices fitness application with a React frontend, secured via Keycloak OAuth2.

## Architecture

```
fitness-app-frontend  (React + Vite, port 5173)
        │
        ▼
    gateway           (Spring Cloud Gateway, port 8080)
    ├── OAuth2 Resource Server (Keycloak JWT validation)
    ├── Keycloak → User sync filter
    └── Routes to:
        ├── userservice      (PostgreSQL, port 8081)
        ├── acticityservice  (MongoDB + RabbitMQ, port 8082)
        └── aiservice        (MongoDB + RabbitMQ + Groq, port 8083)

configserver  (Spring Cloud Config, port 8888)
eureka        (Netflix Eureka, port 8761)
RabbitMQ      (port 5672)
Keycloak      (port 8181, realm: fitness-oauth2)
```

## Services

| Service | Port | Description |
|---|---|---|
| `eureka` | 8761 | Service registry |
| `configserver` | 8888 | Centralised config |
| `gateway` | 8080 | API gateway + auth |
| `userservice` | 8081 | User registration & profiles (PostgreSQL) |
| `acticityservice` | 8082 | Activity tracking (MongoDB) |
| `aiservice` | 8083 | AI recommendations via Groq (MongoDB) |
| `fitness-app-frontend` | 5173 | React frontend |

## Prerequisites

- Java 21
- Maven 3.9+
- Node 18+
- Docker (for PostgreSQL, MongoDB, RabbitMQ, Keycloak)

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Used in | Description |
|---|---|---|
| `DB_URL` | userservice | PostgreSQL JDBC URL |
| `DB_USERNAME` | userservice | PostgreSQL username |
| `DB_PASSWORD` | userservice | PostgreSQL password |
| `GROQ_API_KEY` | aiservice | Groq API key |

## Running Locally

### 1. Start infrastructure

```bash
# PostgreSQL
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=<DB_PASSWORD> -e POSTGRES_DB=fitness_user_db postgres:15

# MongoDB
docker run -d -p 27017:27017 mongo:7

# RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Keycloak
docker run -d -p 8181:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

### 2. Configure Keycloak

- Create realm: `fitness-oauth2`
- Create a client for the frontend with PKCE enabled
- Set valid redirect URIs to `http://localhost:5173/*`

### 3. Start backend services (in order)

```bash
cd eureka        && ./mvnw spring-boot:run
cd configserver  && ./mvnw spring-boot:run
cd userservice   && ./mvnw spring-boot:run
cd acticityservice && ./mvnw spring-boot:run
cd aiservice     && ./mvnw spring-boot:run
cd gateway       && ./mvnw spring-boot:run
```

### 4. Start frontend

```bash
cd fitness-app-frontend
npm install
npm run dev
```

## API Endpoints (via Gateway on port 8080)

All endpoints require a valid Keycloak Bearer token.

| Method | Path | Service |
|---|---|---|
| POST | `/api/users/register` | userservice |
| GET | `/api/users/{userId}` | userservice |
| GET | `/api/users/{userId}/validate` | userservice |
| POST | `/api/activities` | acticityservice |
| GET | `/api/activities` | acticityservice |
| GET | `/api/activities/{id}` | acticityservice |
| GET | `/api/recommendations` | aiservice |
