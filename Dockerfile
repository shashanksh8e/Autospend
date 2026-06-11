FROM eclipse-temurin:22-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean install -DskipTests
EXPOSE 8081
CMD ["java", "-jar", "target/autospend-0.0.1-SNAPSHOT.jar"]