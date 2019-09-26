FROM openjdk:8-jre-alpine
VOLUME /tmp
ARG JAR_FILE

EXPOSE 8082
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]