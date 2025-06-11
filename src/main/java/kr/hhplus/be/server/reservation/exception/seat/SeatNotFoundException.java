package kr.hhplus.be.server.reservation.exception.seat;

public class SeatNotFoundException extends RuntimeException {

    public SeatNotFoundException() {
        super("좌석 정보가 없습니다.");
    }
}
