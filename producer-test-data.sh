docker run \
-i \
--net=confluent \
--rm \
confluentinc/cp-kafka \
kafka-console-producer --request-required-acks 1 \
--broker-list kafka:9092 --property "parse.key=true" --property "key.separator=:" --topic cpu-metrics-topic