FROM maven:3.6.1-jdk-8-alpine AS MAVEN_BUILD
WORKDIR /build

COPY pom.xml .
COPY odmBatch ./odmBatch/
COPY miniloan-xom ./miniloan-xom/
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
RUN mkdir -p res_data
# VOLUME res_data
COPY ./res_data ./res_data
COPY --from=MAVEN_BUILD build/odmBatch/target/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
