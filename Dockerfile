FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY Wallet_ProcesoBaseDatos.zip /tmp/wallet.zip
RUN mkdir -p /wallet && cd /wallet && jar xf /tmp/wallet.zip
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV ORACLE_WALLET_PATH=/app/wallet
COPY --from=build /wallet /app/wallet
COPY --from=build /app/target/formativa-cloud-native-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
