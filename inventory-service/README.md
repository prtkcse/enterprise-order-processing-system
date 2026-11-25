# Inventory Service

## Overview

Inventory Service is a Spring Boot microservice responsible for managing product stock, processing inventory reservations, releasing reserved stock, and publishing inventory-related business events.

It is part of the Enterprise Order Processing Platform and communicates asynchronously with Order Service and Payment Service using Apache Kafka.

## Technology Stack

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- Hibernate
- Oracle SQL
- Apache Kafka
- Spring Cloud Netflix Eureka
- HikariCP
- Springdoc OpenAPI
- Spring Boot Actuator
- JUnit 5
- Mockito

## Features

- Create product inventory
- Retrieve inventory by product ID
- Retrieve paginated inventory
- Update stock quantities
- Reserve stock for orders
- Release reserved stock
- Atomic multi-product reservation
- Pessimistic database locking
- Duplicate order protection
- Transactional Outbox Pattern
- Scheduled Kafka event publisher
- Kafka event consumers
- Exponential retry backoff
- Stale Outbox event recovery
- Centralized exception handling
- Swagger API documentation
- Actuator health monitoring

## Architecture

Order Service publishes `order.created`.

Inventory Service consumes the event and validates the requested stock.

If all products have sufficient inventory:

- Stock is reserved.
- An inventory reservation record is created.
- `inventory.reserved` is persisted in the Outbox.
- The Outbox Publisher publishes the event to Kafka.

If any product has insufficient stock:

- No stock is deducted.
- The reservation is marked FAILED.
- `inventory.failed` is persisted and published.

## Database Tables

Oracle schema: `INVENTORY_DB`

Tables:

- PRODUCT_INVENTORY
- INVENTORY_RESERVATIONS
- INVENTORY_RESERVATION_ITEMS
- OUTBOX_EVENTS

## Inventory Balance

`TOTAL_STOCK = AVAILABLE_STOCK + RESERVED_STOCK`

Reserved stock remains part of total stock until a separate fulfillment or consumption operation is implemented.

## REST APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/inventory` | Create inventory |
| GET | `/api/inventory` | Get paginated inventory |
| GET | `/api/inventory/{productId}` | Get product inventory |
| PUT | `/api/inventory/{productId}` | Update stock |
| GET | `/api/inventory/reservations/{orderId}` | Get reservation |

## Kafka Topics

### Incoming

- `order.created`
- `inventory.release.requested`

### Outgoing

- `inventory.reserved`
- `inventory.failed`
- `inventory.released`

## Transactional Outbox

Inventory Service stores outgoing events in Oracle before publishing them to Kafka.

This ensures that stock changes and their corresponding outgoing events are committed atomically.

Delivery semantics: at least once.

Consumers must support idempotent event processing.

## Concurrency Management

Inventory Service uses pessimistic write locking to prevent concurrent reservations from overselling stock.

Rows are locked in sorted product ID order to reduce deadlock risk.

## Configuration

Environment variables:

- `INVENTORY_DB_URL`
- `INVENTORY_DB_USERNAME`
- `INVENTORY_DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `EUREKA_URL`

Default Oracle connection:

`jdbc:oracle:thin:@//localhost:1521/FREEPDB1`

## Swagger

`http://localhost:8082/swagger-ui.html`

## Actuator

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/info`

## Running Locally

1. Start Oracle Database.
2. Start Kafka.
3. Start Eureka Discovery Server.
4. Create INVENTORY_DB schema.
5. Execute schema.sql.
6. Configure environment variables.
7. Start Inventory Service.

```bash
mvn -pl inventory-service spring-boot:run
```

## Testing

```bash
mvn -pl inventory-service test
```

## Build

```bash
mvn clean verify
```

## Design Patterns

- Layered Architecture
- Repository Pattern
- DTO Pattern
- Transactional Outbox
- Event-Driven Architecture
- Saga Compensation
- Pessimistic Locking
- Idempotent Processing
- Database Transaction Management
