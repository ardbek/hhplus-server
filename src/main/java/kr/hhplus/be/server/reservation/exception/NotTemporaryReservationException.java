package kr.hhplus.be.server.reservation.exception;

public class NotTemporaryReservationException extends RuntimeException {

    public NotTemporaryReservationException() {
        super("임시예약 상태가 아닙니다.");
    }
}
