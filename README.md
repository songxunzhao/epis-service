# EPIS-service

[![CircleCI](https://circleci.com/gh/TulevaEE/epis-service/tree/master.svg?style=shield)](https://circleci.com/gh/TulevaEE/epis-service/tree/master)

## How to run?

1. setup environment variables: `MHUB_KEYSTORE`, `MHUB_KEYSTORE_PASSWORD`, `QUOTAGUARDSTATIC_URL`, `SOCKS_PROXY_URL`
2. run onboarding-service PostgreSQL database (`docker-compose up`  in the onboarding-service project)
3. run RabbitMQ (`docker-compose up` in the epis-service project)
4. `./gradlew bootRun` in the epis-service project

> Warning! You __must__ have Java __assertions disabled__, otherwise the IBM MQ Factory fails (due to a buggy assertion in their code). Make sure you're not enabling assertions by using the `-ea` command-line switch (which IntelliJ enables by default in Test run configurations).

## Tech stack

**Database:**
onboarding-service PostgreSQL - legacy solution, should have its own DB in the future

**Message Queue:**
RabbitMQ

Running locally with docker:
```
docker-compose up
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
