package kr.hhplus.be.server.queue.exception;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException() {
        super("유효하지 않은 토큰입니다.");
    }
}
