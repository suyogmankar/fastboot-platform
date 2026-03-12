FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/fastboot*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]