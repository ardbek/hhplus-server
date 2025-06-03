package kr.hhplus.be.server.wallet.exception;

public class InvalidChargeAmountException extends RuntimeException {

    public InvalidChargeAmountException() {
        super("1회 충전 금액은 1원 이상, 2,000,000원 이하만 가능합니다.");
    }
}
