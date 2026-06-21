FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

CMD ["java", "-jar", "target/webapp-0.0.1-SNAPSHOT.jar"]
