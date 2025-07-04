package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;

public class ConcertSchedule {

    private final Long id;
    private final Long concertId;
    private final LocalDateTime startAt;
    private final LocalDateTime ticketOpenTime;

    private ConcertSchedule(Builder builder) {
        this.id = builder.id;
        this.concertId = builder.concertId;
        this.startAt = builder.startAt;
        this.ticketOpenTime = builder.ticketOpenTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getConcertId() {
        return concertId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getTicketOpenTime() {
        return ticketOpenTime;
    }

    public static class Builder {
        private Long id;
        private Long concertId;
        private LocalDateTime startAt;
        private LocalDateTime ticketOpenTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder concertId(Long concertId) {
            this.concertId = concertId;
            return this;
        }

        public Builder startAt(LocalDateTime startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder ticketOpenTime(LocalDateTime ticketOpenTime) {
            this.ticketOpenTime = ticketOpenTime;
            return this;
        }

        public ConcertSchedule build() {
            return new ConcertSchedule(this);
        }
    }
}