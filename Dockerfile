FROM openjdk:17
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xms1g", "-Xmx1g", "-jar", "/app.jar"]

