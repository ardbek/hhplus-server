package kr.hhplus.be.server.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException() {
        super("예약이 존재하지 않습니다.");
    }
}
