docker network rm confluent

docker rm -f kafka zookeeper

docker network create confluent

docker run -d \
  --net=confluent \
  --name=zookeeper \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper

  docker run -d \
  --net=confluent \
  --name=kafka \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka

sleep 5

  docker run \
--net=confluent \
--rm confluentinc/cp-kafka \
kafka-topics --create --topic cpu-metrics-topic --partitions 2 --replication-factor 1 \
--if-not-exists --zookeeper zookeeper:2181