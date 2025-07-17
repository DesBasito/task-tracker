FROM maven:3.9.8-amazoncorretto-17 AS build
WORKDIR /build

# Копируем только файлы для загрузки зависимостей (для лучшего кэширования)
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Загружаем зависимости (этот слой будет кэшироваться пока pom.xml не изменится)
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -e -DskipTests

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /build/target/task-tracker*jar ./task-tracker.jar
COPY ./config /app/config

EXPOSE 9778

CMD ["java", "-jar", "task-tracker.jar", "--spring.config.location=file:/app/config/application-prod.yml"]