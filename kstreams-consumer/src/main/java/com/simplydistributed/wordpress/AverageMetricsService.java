package com.simplydistributed.wordpress;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.ValueMapperWithKey;
import org.apache.kafka.streams.state.KeyValueStore;

public final class AverageMetricsService {

    static String SOURCE_TOPIC = System.getenv().getOrDefault("SOURCE_TOPIC", "cpu-metrics-topic");
    static String STATE_STORE_NAME = System.getenv().getOrDefault("STATE_STORE_NAME", "average-metrics-store");
    static String APP_ID = "cpu-metrics-app";
    static String KAFKA_BROKER = System.getenv().getOrDefault("KAFKA_BROKER", "localhost:9092");
    private static final Logger LOGGER = Logger.getLogger(AverageMetricsService.class.getSimpleName());

    public static KafkaStreams startStream() {

        Properties configurations = new Properties();

        configurations.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);

        String streamsAppServerConfig = GlobalAppState.getInstance().getHostPortInfo().host() + ":"
                + GlobalAppState.getInstance().getHostPortInfo().port();

        configurations.put(StreamsConfig.APPLICATION_SERVER_CONFIG, streamsAppServerConfig);

        LOGGER.log(Level.INFO, "Kafa broker --- {0}", KAFKA_BROKER);
        configurations.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);

        configurations.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        configurations.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> metricsStream = builder.stream(SOURCE_TOPIC);

        KTable<String, MetricsCountAndSum> countAndSumAggregate = metricsStream.groupByKey()
                .aggregate(new Initializer<MetricsCountAndSum>() {
                    @Override
                    public MetricsCountAndSum apply() {
                        return new MetricsCountAndSum(0L, 0L);
                    }
                }, new Aggregator<String, String, MetricsCountAndSum>() {
                    @Override
                    public MetricsCountAndSum apply(String key, String value, MetricsCountAndSum current) {
                        Long newCount = current.getCount() + 1;
                        Long newSum = current.getSum() + Long.valueOf(value);
                        MetricsCountAndSum metricsCountAndSum = new MetricsCountAndSum(newCount, newSum);
                        System.out.println("MetricsCountAndSum Aggregate for machine " + key + " = " + metricsCountAndSum);
                        return metricsCountAndSum;
                    }
                },
                Materialized.with(Serdes.String(), new MetricsCountAndSumSerde()));
        
        countAndSumAggregate.mapValues(new ValueMapperWithKey<String, MetricsCountAndSum, Double>() {
            @Override
            public Double apply(String key, MetricsCountAndSum countAndSum) {
                Double average = countAndSum.getSum() / (double) countAndSum.getCount();
                System.out.println("Average so far for machine " + key + " is " + average);
                return average;
            }
        }, Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(STATE_STORE_NAME).withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Double()));

        KafkaStreams metricsAverageStream = new KafkaStreams(builder.build(), configurations);

        metricsAverageStream.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.log(Level.SEVERE, "metricsAverageStream Uncaught exception in Thread {0} - {1}", new Object[]{t, e.getMessage()});
                e.printStackTrace();
            }
        });

        metricsAverageStream.start();
        System.out.println("Started Metrics average stream...");

        return metricsAverageStream;

    }

    static class MetricsCountAndSum {

        private final Long count;
        private final Long sum;

        public MetricsCountAndSum(Long count, Long sum) {
            this.count = count;
            this.sum = sum;
        }

        public Long getCount() {
            return count;
        }

        public Long getSum() {
            return sum;
        }

        @Override
        public String toString() {
            return "count = " + count + " and sum = " + sum;
        }

    }

    static class MetricsCountAndSumSerde implements Serde<MetricsCountAndSum> {

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        @Override
        public void close() {
        }

        @Override
        public Serializer<MetricsCountAndSum> serializer() {
            return new Serializer<MetricsCountAndSum>() {
                @Override
                public void configure(Map<String, ?> configs, boolean isKey) {
                }

                @Override
                public byte[] serialize(String topic, MetricsCountAndSum data) {
                    String countAndSum = data.getCount() + ":" + data.getSum();
                    System.out.println("serialized MetricsCountAndSum " + data + " to bytes");
                    return countAndSum.getBytes();
                }

                @Override
                public void close() {
                }
            };
        }

        @Override
        public Deserializer<MetricsCountAndSum> deserializer() {
            return new Deserializer<MetricsCountAndSum>() {
                @Override
                public void configure(Map<String, ?> configs, boolean isKey) {
                }

                @Override
                public MetricsCountAndSum deserialize(String topic, byte[] countAndSum) {
                    String countAndSumStr = new String(countAndSum);
                    Long count = Long.valueOf(countAndSumStr.split(":")[0]);
                    Long sum = Long.valueOf(countAndSumStr.split(":")[1]);

                    MetricsCountAndSum countAndSumObject = new MetricsCountAndSum(count, sum);
                    System.out.println("Deserialized byte[] to MetricsCountAndSum " + countAndSumObject);

                    return countAndSumObject;
                }

                @Override
                public void close() {
                }
            };
        }

    }

}
