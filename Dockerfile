FROM openjdk:8-alpine
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]