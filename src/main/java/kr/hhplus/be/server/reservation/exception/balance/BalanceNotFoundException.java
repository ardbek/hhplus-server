package kr.hhplus.be.server.reservation.exception.balance;

public class BalanceNotFoundException extends RuntimeException {

    public BalanceNotFoundException() {
        super("잔액 정보가 없습니다.");
    }
}
