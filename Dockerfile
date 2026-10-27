# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Pehle sirf pom copy karo, taaki dependencies cache ho jayein
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q clean package -DskipTests

# ---------- Run stage (chhoti image, sirf JRE) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/XansaStays-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

EXPOSE 10000
CMD ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-10000} -jar app.jar"]