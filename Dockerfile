# Use stable Java 17 image
FROM eclipse-temurin:17-jdk-jammy

# Copy jar
COPY target/*.jar app.jar

# Run app
ENTRYPOINT ["java","-jar","/app.jar"]