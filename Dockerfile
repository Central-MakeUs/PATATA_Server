FROM eclipse-temurin:17-jdk
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]
