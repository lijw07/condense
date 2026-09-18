FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --create-home condense
COPY --from=build /workspace/target/*.jar app.jar
USER condense
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
