#Step 1: Setup môi trường để chạy code
FROM maven:4.0.0-rc-5-amazoncorretto-21-al2023 AS build

WORKDIR /SWP
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

#Step 2: Tạo image để chạy ứng dụng
#image chứa java 21
FROM amazoncorretto:21-al2-jdk

WORKDIR /SWP
COPY --from=build /SWP/target/*.jar SWP.jar

#Step 3: EntryPoint
ENTRYPOINT ["java","-jar","SWP.jar"]