package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;

public class Payment {

    private final Long id;
    private final Long userId;
    private final Long reservationId;
    private final Long price;
    private final LocalDateTime createdAt;

    private Payment(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.reservationId = builder.reservationId;
        this.price = builder.price;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private Long userId;
        private Long reservationId;
        private Long price;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder reservationId(Long reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public Builder price(Long price) {
            this.price = price;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}