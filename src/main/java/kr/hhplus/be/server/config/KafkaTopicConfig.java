package kr.hhplus.be.server.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String RESERVATION_CONFIRMED_TOPIC = "reservation-confirmed";
    public static final String CONCERT_SOLD_OUT_TOPIC = "concert-sold-out";

    @Bean
    public NewTopic reservationConfirmedTopic() {
        return TopicBuilder.name(RESERVATION_CONFIRMED_TOPIC)
            .partitions(3)
            .replicas(3)
            .build();
    }

    @Bean
    public NewTopic concertSoldOutTopic() {
        return TopicBuilder.name(CONCERT_SOLD_OUT_TOPIC)
            .partitions(3)
            .replicas(3)
            .build();
    }
}