package kr.hhplus.be.server.reservation.exception.balance;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("잔액이 부족합니다.");
    }
}
