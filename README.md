Practical example for Interactive Queries feature in Kafka Streams. It covers the DSL API and how the state store information was exposed via a REST service. Everything is setup using Docker including Kafka, Zookeeper, the stream processing services as well as the producer app

Here are the key components

- Metrics producer service - pushes machine metrics (simulated) to Kafka
- Average Processor service - calculates the average of the stream of generated metrics and exposes REST APIs to query them

![](https://cdn-images-1.medium.com/max/2000/1*dmjneoc5Zjwh4cgL5Vd0mA.jpeg)