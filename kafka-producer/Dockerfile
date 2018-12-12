#just for local development/testing
FROM openjdk:8-jre-alpine
WORKDIR /
COPY target/kafka-cpu-metrics-producer.jar /
CMD ["java", "-jar","kafka-cpu-metrics-producer.jar"]