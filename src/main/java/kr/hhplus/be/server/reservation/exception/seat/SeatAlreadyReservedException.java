package kr.hhplus.be.server.reservation.exception.seat;

public class SeatAlreadyReservedException extends RuntimeException {

    public SeatAlreadyReservedException() {
        super("이미 임시 예약된 좌석입니다.");
    }
}
