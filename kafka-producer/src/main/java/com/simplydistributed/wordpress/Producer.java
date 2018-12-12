package com.simplydistributed.wordpress;

import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Producer implements Runnable {

    static final Logger LOGGER = Logger.getLogger(Producer.class.getName());
    static final String TOPIC_NAME = "cpu-metrics-topic";
    final static String KAFKA_CLUSTER_ENV_VAR_NAME = "KAFKA_BROKER";

    @Override
    public void run() {
        try {
            produce();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    static final String KEY_PREFIX = "machine";
    static final String defaultClusterValue = "localhost:9092";
    static final String kafkaCluster = System.getenv().getOrDefault(KAFKA_CLUSTER_ENV_VAR_NAME, defaultClusterValue);

    //simulate data for 5 machines.. the key will be machine1, machine2, upto machine5
    private void produce() throws Exception {
        ProducerRecord<String, String> record = null;

        LOGGER.log(Level.INFO, "Kafka Producer running in thread {0}", Thread.currentThread().getName());
        Properties kafkaProps = new Properties();
        LOGGER.log(Level.INFO, "Kafka cluster {0}", kafkaCluster);

        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.ACKS_CONFIG, "0");

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProps);

        try {
            Random rnd = new Random();

            for (int i = 1; i <= 5; i++) {
                String key = KEY_PREFIX + i;
                String value = String.valueOf(rnd.nextInt(20));
                record = new ProducerRecord<>(TOPIC_NAME, key, value);

                RecordMetadata rm = kafkaProducer.send(record).get();
                LOGGER.log(Level.INFO, "Partition for key-value {0}::{1} is {2}", new Object[]{key, value, rm.partition()});

            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Producer thread was interrupted");
        } finally {
            kafkaProducer.close();
            LOGGER.log(Level.INFO, "Producer closed");
        }

    }

}
