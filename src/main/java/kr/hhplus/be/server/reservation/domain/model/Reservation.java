package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.exception.reservation.NotTemporaryReservationException;
import kr.hhplus.be.server.reservation.exception.reservation.NotYourReservationException;
import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;

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

    /**
     * 임시 예약
     * @param userId - 예약할 사용자
     * @param seatId - 예약할 좌석
     * @param concertScheduleId - 예약할 콘서트 id
     * @param isExistsLock - 이미 다른 사용자가 예약중인지 여부
     * @return
     */
    public static Reservation reserveTemporary(Long userId, Long seatId, Long concertScheduleId, boolean isExistsLock) {
        if (isExistsLock) {
            throw new SeatAlreadyReservedException();
        }

        return Reservation.builder()
                .userId(userId)
                .concertScheduleId(concertScheduleId)
                .seatId(seatId)
                .status(ReservationStatus.LOCKED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 예약 최종 확정
     * @param requestUserId 결제 시도하는 사용자 ID
     * @throws NotYourReservationException 예약 소유자가 아닐 경우
     * @throws NotTemporaryReservationException 임시 배정(LOCKED) 상태가 아닐 경우
     */
    public void confirm(Long requestUserId) {
        if(!this.userId.equals(requestUserId)) {
            throw new NotYourReservationException();
        }

        if (this.status != ReservationStatus.LOCKED) {
            throw new NotTemporaryReservationException();
        }

        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
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
