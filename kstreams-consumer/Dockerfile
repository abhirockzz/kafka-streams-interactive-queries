#just for local development/testing
FROM openjdk:8-jre
WORKDIR /
COPY target/kafka-cpu-metrics-consumer-1.0.jar /
EXPOSE 8080
CMD ["java", "-jar","kafka-cpu-metrics-consumer-1.0.jar"]