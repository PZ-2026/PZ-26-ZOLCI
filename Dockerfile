# ============================================================
# Obraz produkcyjny — kopiuje gotowy JAR zbudowany lokalnie.
# Przed docker compose up zbuduj backend:
#   cd backend && .\mvnw.cmd package -DskipTests
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Użytkownik bez uprawnień roota
RUN addgroup -S trainit && adduser -S trainit -G trainit
USER trainit

COPY backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
