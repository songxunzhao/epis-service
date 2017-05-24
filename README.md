# EPIS-service

[![CircleCI](https://circleci.com/gh/TulevaEE/epis-service/tree/master.svg?style=shield)](https://circleci.com/gh/TulevaEE/onboarding-service/tree/master)

## Tech stack

**Database:**
PostgreSQL

Running locally with docker:
```
docker run -d --name tuleva-onboarding-database \
                 -p 5432:5432 \
                 -e "POSTGRES_USER=tuleva-onboarding" \
                 -e "POSTGRES_DB=tuleva-onboarding" \
                 postgres:9.6
```

**Backend:**
Java 8, Spring Boot, Gradle, Spock for testing

Uses AMQP for synchronizing messages from EPIS, tested with RabbitMQ.

**Error tracking:**
Rollbar

**Hosting:**
Heroku

For static IP - quotaguard static Heroku plugin

**CI:**
CircleCI