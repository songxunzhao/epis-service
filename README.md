# EPIS-service

[![CircleCI](https://circleci.com/gh/TulevaEE/epis-service/tree/master.svg?style=shield)](https://circleci.com/gh/TulevaEE/epis-service/tree/master)

## How to run?

1. Set up [VPN connection](https://github.com/TulevaEE/tuleva-vpn#openvpn-client-setup)
1. setup environment variables: `MHUB_KEYSTORE`, `MHUB_KEYSTOREPASSWORD`
1. run onboarding-service PostgreSQL database (`docker-compose up`  in the onboarding-service project)
1. run RabbitMQ (`docker-compose up` in the epis-service project)
1. `./gradlew bootRun` in the epis-service project

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
Rollbar

**Hosting:**
AWS

Check `infrastructure` for more info.

**CI:**
CircleCI

## References

Technical documentation of the EPIS API: http://www.pensionikeskus.ee/tech-docs/
