FROM openjdk:21-jdk-slim AS build

WORKDIR /app

COPY . /app

ENV CLASSPATH ".:/app/libs/*"

RUN javac -cp "$CLASSPATH" SODAnalysis.java

FROM openjdk:21-jre-slim

WORKDIR /app

COPY --from=build /app /app

ENTRYPOINT ["java", "SODAnalysis"]
