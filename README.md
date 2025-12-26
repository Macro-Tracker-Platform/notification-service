# Notification Service

[**← Back to Main Architecture**](https://github.com/oleh-prukhnytskyi/macro-tracker)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

---

[![License](https://img.shields.io/badge/license-Apache%202.0-blue?style=for-the-badge)](LICENSE)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-Image-blue?style=for-the-badge&logo=docker)](https://hub.docker.com/repository/docker/olehprukhnytskyi/macro-tracker-notification-service/general)

**Asynchronous Email Delivery System.**

Responsible for handling system notifications (Registration Confirmations, Password Resets) by consuming events from Apache Kafka, decoupling email latency from user-facing services.

## :zap: Service Specifics

* **Event-Driven Architecture**: Operates purely as a Kafka consumer, ensuring that temporary failures in the mail server do not impact the user registration flow or other upstream processes.
* **Stateless Design**: Does not maintain a local database; it relies solely on the payload provided in Kafka events to generate emails.
* **Reliability**: Leverages Kafka consumer groups (`notification-group`) to ensure message delivery guarantees and scalability.
* **Native Templating**: Utilizes Java Text Blocks (JDK 21) for efficient and type-safe HTML email construction without heavy template engines.

---

## :electric_plug: API & Communication

* **Public API**: This service is primarily a background worker and does not expose a REST API for business logic, though it includes Actuator endpoints for health monitoring.
* **Internal Communication**:
    * *Async (Input)*: Consumes messages from the **User Service** via Kafka.

### Kafka Consumers

| Topic | Group ID | Payload Event | Action |
| :--- | :--- | :--- | :--- |
| `user-registered` | `notification-group` | `RegistrationEvent` | Sends a confirmation code email. |
| `user-password-reset` | `notification-group` | `PasswordResetEvent` | Sends a password reset code email. |

---

## :hammer_and_wrench: Tech Details

| Component | Implementation |
| :--- | :--- |
| **Messaging** | Spring Kafka (Consumer) |
| **Email** | JavaMailSender (Spring Boot Starter Mail) |
| **Serialization** | Jackson (JSON) |
| **Monitoring** | Spring Boot Actuator, Logstash Logback Encoder |

---

## :gear: Environment Variables

Required variables for `local` or `k8s` deployment:

| Variable | Purpose |
| :--- | :--- |
| **Mail Server Configuration** | |
| `EMAIL_HOST` | SMTP server host (e.g., `smtp.gmail.com`). |
| `EMAIL_PORT` | SMTP server port (e.g., `587`). |
| `EMAIL_USERNAME` | SMTP username / sender email address. |
| `EMAIL_PASSWORD` | SMTP password or App Password. |
| **Infrastructure** | |
| `KAFKA_URL` | Kafka bootstrap servers address. |
| `MACRO_TRACKER_URL` | Base URL of the application (used for generating links/Swagger config). |

---

## :whale: Quick Start

```bash
# Pull from Docker Hub
docker pull olehprukhnytskyi/macro-tracker-notification-service:latest

# Run (Ensure your .env file contains all required variables listed above)
docker run -p 8080:8080 --env-file .env olehprukhnytskyi/macro-tracker-notification-service:latest
```

---

## :balance_scale: License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.