package kr.hhplus.be.server.wallet.service;

import kr.hhplus.be.server.wallet.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.wallet.dto.response.WalletBalanceResponse;

public interface WalletService {

    WalletBalanceResponse getBalance(Long userId);

    BalanceChargeResponse charge(Long walletId, long chargeAmount);
}
