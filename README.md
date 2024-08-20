## General info
This project is an example of of Spring Boot CRUD application using 
REST with HATEOAS and docker containers to setup up and deploy app with postgres database.

## Local run
To run application on local machine set "local" profile as program arguments:
```
--spring.profiles.active=local
```

## Docker run
To run this project od docker build jar and run docker compose:
```
mvn clean install
docker compose up
```