APP_NAME=metricsproducer
mvn clean install -f kafka-producer/pom.xml

docker build -t $APP_NAME kafka-producer

rm -rf kafka-producer/target

  docker run --rm -d \
  --net=confluent \
  -e KAFKA_BROKER=kafka:9092 \
  --name=$APP_NAME \
  $APP_NAME