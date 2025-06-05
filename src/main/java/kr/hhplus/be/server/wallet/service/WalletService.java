package kr.hhplus.be.server.wallet.service;

import kr.hhplus.be.server.wallet.domain.Wallet;

public interface WalletService {

    Wallet getBalance(Long userId);

    Wallet charge(Long walletId, long chargeAmount);
}
