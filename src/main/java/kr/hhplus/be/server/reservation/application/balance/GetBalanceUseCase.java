package kr.hhplus.be.server.reservation.application.balance;

import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.exception.balance.BalanceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class GetBalanceUseCase {

    private final BalanceRepository balanceRepository;

    public GetBalanceUseCase(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Transactional(readOnly = true)
    public Balance getBalance(Long userId) {
        return balanceRepository.findByUserId(userId)
                .orElseThrow(BalanceNotFoundException::new);
    }
}
