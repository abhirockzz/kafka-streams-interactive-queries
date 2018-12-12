docker run \
-i \
--net=confluent \
--rm \
confluentinc/cp-kafka \
kafka-console-producer --request-required-acks 1 \
--broker-list kafka:9092 --property "parse.key=true" --property "key.separator=:" --topic cpu-metrics-topic


# docker run -i --net=confluent --rm confluentinc/cp-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic hashtag-count-topic --from-beginning --property print.key=true