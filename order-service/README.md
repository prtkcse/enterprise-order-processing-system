# Order Service

## Overview

Order Service is a Spring Boot microservice responsible for order creation, retrieval, lifecycle management, and asynchronous business event processing in the Enterprise Order Processing Platform.

It uses Oracle SQL for persistence and Apache Kafka for asynchronous communication with Inventory, Payment, and Notification services.

## Architecture

- Spring Boot REST APIs
- Spring Data JPA
- Oracle SQL
- Apache Kafka
- Eureka Service Discovery
- Transactional Outbox Pattern
- Spring Boot Actuator
- OpenAPI / Swagger UI

## Features

- Create orders
- Retrieve orders by ID
- Retrieve paginated orders
- Retrieve customer orders
- Cancel eligible orders
- Calculate order totals
- Validate incoming requests
- Persist orders and items
- Transactional outbox event persistence
- Scheduled Kafka event publisher
- Kafka event consumers
- Retry with exponential backoff
- Optimistic locking
- Centralized exception handling
- Swagger documentation
- Health checks and metrics

## APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders/{orderId}` | Get order |
| GET | `/api/orders` | Get paginated orders |
| GET | `/api/orders/customer/{customerId}` | Get customer orders |
| PATCH | `/api/orders/{orderId}/cancel` | Cancel an order |
| GET | `/api/orders/{orderId}/status` | Get order status |

## Order Lifecycle

`PENDING → INVENTORY_RESERVED → PAYMENT_PROCESSING → CONFIRMED`

Failure states:

- INVENTORY_FAILED
- PAYMENT_FAILED
- CANCELLED

## Transactional Outbox

Order creation writes the order, order items, and event payload into Oracle within the same database transaction.

A scheduled Outbox Publisher retrieves pending records and publishes them to Kafka.

Outbox statuses:

- PENDING
- PROCESSING
- PUBLISHED
- FAILED

Kafka publication uses at-least-once delivery semantics. Consumers must implement idempotency.

## Kafka Topics

### Outgoing

- `order.created`
- `order.confirmed`
- `order.failed`
- `payment.requested`
- `inventory.release.requested`
- `order.cancelled`

### Incoming

- `inventory.reserved`
- `inventory.failed`
- `payment.completed`
- `payment.failed`

## Database

Oracle schema: `ORDER_DB`

Tables:

- ORDERS
- ORDER_ITEMS
- OUTBOX_EVENTS

## Configuration

Environment variables:

- `ORDER_DB_URL`
- `ORDER_DB_USERNAME`
- `ORDER_DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`

Default Oracle URL:

`jdbc:oracle:thin:@//localhost:1521/FREEPDB1`

## Swagger

`http://localhost:8081/swagger-ui.html`

OpenAPI:

`http://localhost:8081/v3/api-docs`

## Actuator

- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/info`

## Running locally

1. Start Oracle.
2. Start Kafka.
3. Start Eureka Discovery Server.
4. Create the `ORDER_DB` schema.
5. Execute `schema.sql`.
6. Configure the database environment variables.
7. Run the Order Service.

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```

## Build

From the root of the multi-module repository:

```bash
mvn clean verify
```

## Design patterns

- Layered Architecture
- Repository Pattern
- DTO Pattern
- Transactional Outbox
- Event-Driven Architecture
- Saga-style Workflow
- Optimistic Locking
- Idempotent Consumer Design
