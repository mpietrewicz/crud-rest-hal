FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY ./target/crud-rest-hal-1.0-SNAPSHOT.jar /opt/app
CMD ["java", "-jar", "/opt/app/crud-rest-hal-1.0-SNAPSHOT.jar"]