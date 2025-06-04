package kr.hhplus.be.server.reservation.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;

@Entity
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;
    Long concertScheduleId;
    Long seatId;

    @Enumerated(EnumType.STRING)
    ReservationStatus status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    protected ReservationEntity() {}

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getConcertScheduleId() {
        return concertScheduleId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
