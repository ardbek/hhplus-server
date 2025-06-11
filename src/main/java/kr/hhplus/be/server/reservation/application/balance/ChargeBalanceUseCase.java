package kr.hhplus.be.server.reservation.application.balance;

import kr.hhplus.be.server.queue.exception.balance.WalletNotFoundException;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import org.springframework.transaction.annotation.Transactional;

public class ChargeBalanceUseCase {

    private final BalanceRepository balanceRepository;

    public ChargeBalanceUseCase(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Transactional
    public Balance charge(Long userId, Long chargeAmount) {

        // 잔고 조회 (비관적 락)
        Balance target = balanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        Balance charged = target.charge(chargeAmount);

        return balanceRepository.save(charged);

    }
}
