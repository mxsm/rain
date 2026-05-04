FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -q -Dflatten.skip=true -DskipTests -pl rain-uidgenerator-server -am package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN addgroup --system rain && adduser --system --ingroup rain rain
COPY --from=build /workspace/rain-uidgenerator-server/target/rain-uidgenerator-server-*.jar /app/rain-server.jar
USER rain
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+ExitOnOutOfMemoryError","-jar","/app/rain-server.jar"]
