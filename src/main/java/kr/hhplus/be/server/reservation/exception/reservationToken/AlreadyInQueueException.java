package kr.hhplus.be.server.reservation.exception.reservationToken;

public class AlreadyInQueueException extends RuntimeException {

    public AlreadyInQueueException() {
        super("이미 대기열에 있습니다.");
    }
}
