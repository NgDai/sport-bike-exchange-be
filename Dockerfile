FROM maven:4.0.0-rc-5-amazoncorretto-21-al2023 AS build

WORKDIR /SWP
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-al2-jdk

WORKDIR /SWP
COPY --from=build /SWP/target/*.jar SWP.jar

ENTRYPOINT ["java","-jar","SWP.jar"]