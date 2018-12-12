package com.simplydistributed.wordpress;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 * Entry point for the application. Kicks off Kafka Producer thread
 */
public class ProducerBootstrap {

    private static final Logger LOGGER = Logger.getLogger(ProducerBootstrap.class.getName());

    public static void main(String[] args) throws Exception {

        ScheduledExecutorService kafkaProducerScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        
        //run producer after every 10 seconds
        kafkaProducerScheduledExecutor.scheduleWithFixedDelay(new Producer(), 5, 10, TimeUnit.SECONDS);
        LOGGER.info("Kafka producer triggered");

    }
}
