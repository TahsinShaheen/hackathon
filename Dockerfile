



FROM openjdk:21-slim AS build

WORKDIR /app

COPY . .

RUN javac -cp ".:libs/*" SODAnalysis.java

FROM openjdk:21-slim

WORKDIR /app

COPY --from=build /app /app

ENTRYPOINT ["java", "-cp", ".:libs/*", "SODAnalysis"]
