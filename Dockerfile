FROM openjdk:16-jdk-alpine
MAINTAINER Benoit Surprenant
COPY build/libs/apple-music-stats-interaction-0.0.1-SNAPSHOT.jar apple-music-stats-interaction-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/apple-music-stats-interaction-0.0.1-SNAPSHOT.jar"]