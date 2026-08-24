FROM maven:3.9.16-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/ticket-management-0.0.1-SNAPSHOT.jar"]
