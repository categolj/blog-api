FROM openjdk:11-slim
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]