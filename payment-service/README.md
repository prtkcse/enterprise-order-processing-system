# Payment Service

## Overview

Payment Service is responsible for processing payments in the Enterprise Order Processing Platform.

It consumes inventory reservation events, initiates payment processing, stores payment transaction details in Oracle Database, and publishes payment result events through Kafka using the Transactional Outbox Pattern.

## Technology Stack

- Java
- Spring Boot
- Spring Data JPA
- Oracle Database
- Apache Kafka
- Spring Cloud Netflix Eureka
- Maven
- Swagger/OpenAPI
- Actuator
- Prometheus/Micrometer

## Features

- Payment initiation
- Simulated payment gateway
- Payment success and failure handling
- Idempotent payment processing
- Kafka event consumption
- Kafka event production
- Transactional Outbox Pattern
- Retry handling
- Oracle persistence
- Eureka registration
- Swagger documentation
- Actuator monitoring

## Kafka Topics

### Consumed

- inventory.reserved

### Produced

- payment.completed
- payment.failed

## Database Tables

- PAYMENTS
- OUTBOX_EVENTS

## APIs

### Initiate Payment

POST `/api/payments`

### Retrieve Payment

GET `/api/payments/order/{orderId}`

## Swagger

http://localhost:8083/swagger-ui.html

## Actuator

http://localhost:8083/actuator/health

## Prometheus

http://localhost:8083/actuator/prometheus

## Configuration

Database configuration, Kafka bootstrap servers, Eureka URL, gateway simulation success rate and Outbox settings are configurable through `application.properties` or environment variables.

## Payment Flow

1. Order Service publishes `order.created`.
2. Inventory Service reserves stock.
3. Inventory Service publishes `inventory.reserved`.
4. Payment Service processes the payment.
5. Payment Service publishes `payment.completed` or `payment.failed`.
6. Order Service updates the order status.
7. If payment fails, Order Service requests inventory release.

## Reliability

Payment result events are persisted in the Outbox table before being published to Kafka.

The Outbox Publisher publishes pending events and updates their status after Kafka acknowledges delivery.

Payment processing uses idempotency to prevent duplicate payment records for the same order.
