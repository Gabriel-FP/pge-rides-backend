# Rides Backend

Simplified ride-hailing API built with Java 17 and Spring Boot 3.

## Tech Stack

- **Java 17** / **Spring Boot 3.5**
- **PostgreSQL 15** — persistence
- **RabbitMQ 3** — async messaging between ride events
- **Redis 7** — in-progress ride status cache
- **SSE** — real-time driver notifications

## Prerequisites

- **Docker** and **Docker Compose** (recommended)
- **Java 17** and **Maven 3.9+** (only for local development without Docker)

## Environment Variables

Copy the template and set your database password:

```bash
cp .env.example .env
```

| Variable | Default | Description |
|---|---|---|
| `DB_NAME` | `rides_db` | PostgreSQL database name |
| `DB_USER` | `rides_app` | PostgreSQL username |
| `DB_PASSWORD` | — | PostgreSQL password (**required**) |

## Running with Docker

Start the full stack (PostgreSQL + RabbitMQ + Redis + backend) with a single command:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

## Running Locally

```bash
# 1. Start infrastructure services
docker compose up -d postgres rabbitmq redis

# 2. Create .env from the template
cp .env.example .env
# Edit .env and set DB_PASSWORD

# 3. Run the application
./mvnw spring-boot:run
```

## Running Tests

```bash
./mvnw test
```

21 unit tests covering service, scheduler, mapper, and notification layers.

## API Endpoints

| Method | Endpoint | Description | Responses |
|---|---|---|---|
| `POST` | `/rides` | Create a ride | 201, 400, 409 |
| `GET` | `/rides` | List all rides | 200 |
| `PATCH` | `/rides/{id}/accept?driverId=X` | Accept a ride | 200, 404, 409 |
| `GET` | `/rides/{id}/status` | Get ride status (cached) | 200, 404 |
| `GET` | `/notifications/stream?driverId=X` | SSE stream for drivers | event: new-ride |

## Project Structure

```
br.gov.pge.rides
├── controller/      REST controllers (Ride, Notification)
├── service/         Business logic
├── mapper/          DTO ↔ Entity mapping
├── repository/      Spring Data JPA repositories
├── model/           JPA entities and enums
├── dto/             Request and response DTOs
├── exception/       Global exception handler and custom exceptions
├── messaging/       RabbitMQ producer and consumer
├── notification/    SSE-based driver notifications
├── cache/           Redis caching layer
├── config/          RabbitMQ and CORS configuration
└── scheduler/       Ride timeout scheduler
```
