package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;

public class Reservation {

    private Long id;
    private Long userId;
    private Long concertScheduleId;
    private Long seatId;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Reservation(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.concertScheduleId = builder.concertScheduleId;
        this.seatId = builder.seatId;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isLocked() {
        return this.status == ReservationStatus.LOCKED;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private Long concertScheduleId;
        private Long seatId;
        private ReservationStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id; return this;
        }
        public Builder userId(Long userId) {
            this.userId = userId; return this;
        }
        public Builder concertScheduleId(Long concertScheduleId) {
            this.concertScheduleId = concertScheduleId; return this;
        }
        public Builder seatId(Long seatId) {
            this.seatId = seatId; return this;
        }
        public Builder status(ReservationStatus status) {
            this.status = status; return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt; return this;
        }
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt; return this;
        }
        public Reservation build() {
            return new Reservation(this);
        }
    }

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
