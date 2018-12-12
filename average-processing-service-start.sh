APP_NAME=averageprocessorservice
mvn clean install -f kstreams-consumer/pom.xml
lastapp=$(docker ps -a -f name=$APP_NAME* -l --format '{{.Names}}')
#echo $lastapp
IFS='-' read -ra lastapparray <<< "$lastapp" 
lastindex=${lastapparray[1]}
#echo "lastindex -- " $lastindex
currentindex=$((lastindex+1))
#echo "currentindex -- " $currentindex

docker build -t $APP_NAME kstreams-consumer

rm -rf kstreams-consumer/target

  docker run --rm -d \
  --net=confluent \
  -e KAFKA_BROKER=kafka:9092 \
  --name=$APP_NAME-$currentindex \
  -P \
  $APP_NAME