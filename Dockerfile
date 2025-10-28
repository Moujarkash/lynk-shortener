FROM gradle:8.14.3-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/lynk_shortener.jar

ENTRYPOINT ["java", "-jar", "/app/lynk_shortener.jar"]