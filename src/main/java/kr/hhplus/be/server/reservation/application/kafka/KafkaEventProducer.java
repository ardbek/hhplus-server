package kr.hhplus.be.server.reservation.application.kafka;

import static kr.hhplus.be.server.config.KafkaTopicConfig.CONCERT_SOLD_OUT_TOPIC;
import static kr.hhplus.be.server.config.KafkaTopicConfig.RESERVATION_CONFIRMED_TOPIC;

import kr.hhplus.be.server.reservation.domain.event.ConcertSoldOutEvent;
import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * ReservationConfirmedEvent를 Kafka로 발행
     */
    public void sendReservationConfirmedEvent(ReservationConfirmedEvent event) {
        kafkaTemplate.send(RESERVATION_CONFIRMED_TOPIC, String.valueOf(event.reservationId()), event);
    }

    /**
     * ConcertSoldOutEvent를 Kafka로 발행
     */
    public void sendConcertSoldOutEvent(ConcertSoldOutEvent event) {
        kafkaTemplate.send(CONCERT_SOLD_OUT_TOPIC, String.valueOf(event.concertScheduleId()), event);
    }
}