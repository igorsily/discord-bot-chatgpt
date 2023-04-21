FROM openjdk:17-jdk-slim

VOLUME /tmp

ARG REGION_ARG=sa-east-1
ARG ACCESS_ARG
ARG SECRET_ARG
ENV AWS_REGION=$REGION_ARG
ENV AWS_ACCESS_KEY=$ACCESS_ARG
ENV AWS_SECRET_KEY=$SECRET_ARG

ARG JAR_FILE=target/*.jar

# Copying JAR file
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
