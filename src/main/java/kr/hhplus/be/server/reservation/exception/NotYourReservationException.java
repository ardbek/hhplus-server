package kr.hhplus.be.server.reservation.exception;

public class NotYourReservationException extends RuntimeException {

    public NotYourReservationException() {
        super("본인 예약이 아닙니다.");
    }
}
