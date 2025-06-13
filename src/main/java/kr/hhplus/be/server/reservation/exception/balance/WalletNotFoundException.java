package kr.hhplus.be.server.reservation.exception.balance;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)/*ControllerAdvice 없음.*/
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long walletId) {
        super("지갑을 찾을 수 없습니다. walletId = "+ walletId);
    }
}
