package kr.hhplus.be.server.reservation.domain.model;

import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;

public class Seat {

    private final Long id;
    private final Long concertScheduleId;
    private final Integer seatNo;
    private final Long price;
    private SeatStatus status;

    private Seat(Builder builder) {
        this.id = builder.id;
        this.concertScheduleId = builder.concertScheduleId;
        this.seatNo = builder.seatNo;
        this.price = builder.price;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 좌석 예약
     */
    public void reserve() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new SeatAlreadyReservedException();
        }
        this.status = SeatStatus.RESERVED;
    }

    /**
     * 예약 가능한 상태로 변경
     */
    public void makeAvailable() {
        if (this.status == SeatStatus.RESERVED) {
            this.status = SeatStatus.AVAILABLE;
        }
    }

    public Long getId() {
        return id;
    }

    public Long getConcertScheduleId() {
        return concertScheduleId;
    }

    public Integer getSeatNo() {
        return seatNo;
    }

    public Long getPrice() {
        return price;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public static class Builder {
        private Long id;
        private Long concertScheduleId;
        private Integer seatNo;
        private Long price;
        private SeatStatus status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder concertScheduleId(Long concertScheduleId) {
            this.concertScheduleId = concertScheduleId;
            return this;
        }

        public Builder seatNo(Integer seatNo) {
            this.seatNo = seatNo;
            return this;
        }

        public Builder price(Long price) {
            this.price = price;
            return this;
        }

        public Builder status(SeatStatus status) {
            this.status = status;
            return this;
        }

        public Seat build() {
            if (status == null) {
                this.status = SeatStatus.AVAILABLE;
            }
            return new Seat(this);
        }
    }

    public enum SeatStatus {
        AVAILABLE, // 예약 가능
        RESERVED,  // 임시 예약됨 (결제 대기)
        PAID       // 결제 완료
    }
}