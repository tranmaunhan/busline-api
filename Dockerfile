FROM eclipse-temurin:21-jdk

WORKDIR /app

ENV TZ=Asia/Ho_Chi_Minh

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Duser.timezone=Asia/Ho_Chi_Minh","-jar","app.jar"]
