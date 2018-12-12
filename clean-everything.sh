docker rm -f zookeeper kafka
docker rm -f $(docker ps -aq -f name=metricsproducer)
docker rm -f $(docker ps -aq -f name=averageprocessorservice-*)
docker network rm confluent