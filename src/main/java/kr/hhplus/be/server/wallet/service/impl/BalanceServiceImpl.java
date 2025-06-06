package kr.hhplus.be.server.wallet.service.impl;

import kr.hhplus.be.server.wallet.domain.Balance;
import kr.hhplus.be.server.wallet.exception.InvalidChargeAmountException;
import kr.hhplus.be.server.wallet.exception.WalletNotFoundException;
import kr.hhplus.be.server.wallet.repository.BalanceRepository;
import kr.hhplus.be.server.wallet.service.BalanceService;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;

    public BalanceServiceImpl(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public Balance getBalance(Long walletId) {
        return balanceRepository.findByUserId(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Override
    public Balance charge(Long walletId, long chargeAmount) {
        if (chargeAmount <= 0 || chargeAmount > 2_000_000L) {
            throw new InvalidChargeAmountException();
        }

        Balance target = balanceRepository.findByIdForUpdate(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));


        Balance charged = target.charge(chargeAmount);
        return balanceRepository.save(charged);
    }
}
