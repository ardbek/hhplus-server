package kr.hhplus.be.server.reservation.exception.balance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(HttpStatus.NOT_FOUND)/*ControllerAdvice 없음.*/
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long walletId) {
        super("지갑을 찾을 수 없습니다. walletId = "+ walletId);
        log.error("잔액 충전 오류 :: walletId = {}", walletId);
    }
}
