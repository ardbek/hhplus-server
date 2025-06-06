package kr.hhplus.be.server.wallet.service;

import kr.hhplus.be.server.wallet.domain.Balance;

public interface BalanceService {

    Balance getBalance(Long userId);

    Balance charge(Long walletId, long chargeAmount);
}
