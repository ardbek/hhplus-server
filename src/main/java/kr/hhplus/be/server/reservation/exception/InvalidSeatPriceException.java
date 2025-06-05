package kr.hhplus.be.server.reservation.exception;

public class InvalidSeatPriceException extends RuntimeException {

    public InvalidSeatPriceException() {
        super("좌석 금액이 올바르지 않습니다.");
    }
}
