# EPIS-service

[![CircleCI](https://circleci.com/gh/TulevaEE/epis-service/tree/master.svg?style=shield)](https://circleci.com/gh/TulevaEE/epis-service/tree/master)
[![codecov](https://codecov.io/gh/TulevaEE/epis-service/branch/master/graph/badge.svg)](https://codecov.io/gh/TulevaEE/epis-service)

## How to run?

1. Set up [VPN connection](https://github.com/TulevaEE/tuleva-vpn#openvpn-client-setup)
1. Download `testkeystore.p12` and save it to `./test_keys/testkeystore.p12`
1. Setup environment variables: `MHUB_KEYSTORE`, `MHUB_KEYSTOREPASSWORD`, `MHUB_USERID`, `MHUB_PASSWORD` (ask from your fellow engineers)
1. Run RabbitMQ (`docker-compose up` in the epis-service project directory)
1. Run `./gradlew wsdl2java` to generate Java classes from the WSDL files
1. `./gradlew bootRun` in the epis-service project directory

> Warning! You __must__ have Java __assertions disabled__, otherwise the IBM MQ Factory fails (due to a buggy assertion in their code). Make sure you're not enabling assertions by using the `-ea` command-line switch (which IntelliJ enables by default in Test run configurations).

## Single Dev Rule

Only a __single__ developer machine can be __connected to EPIS__ simultaneously. Why? Since we're using a single whitelisted IP to communicate with EPIS and >1 machines would be in a race condition in getting the response back from the MQ.

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
Sentry

**Hosting:**
AWS

Check `infrastructure` for more info.

**CI:**
CircleCI

## References

Technical documentation of the EPIS API: http://www.pensionikeskus.ee/tech-docs/

Ask for the password.
