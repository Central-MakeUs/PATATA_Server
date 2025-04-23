FROM openjdk:17
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app.jar"]