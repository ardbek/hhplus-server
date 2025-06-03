package kr.hhplus.be.server.wallet.service.impl;

import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.wallet.dto.response.WalletBalanceResponse;
import kr.hhplus.be.server.wallet.exception.WalletNotFoundException;
import kr.hhplus.be.server.wallet.repository.WalletRepository;
import kr.hhplus.be.server.wallet.service.WalletService;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public WalletBalanceResponse getBalance(Long walletId) {
        Wallet findWallet = walletRepository.findByUserId(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        return new WalletBalanceResponse(findWallet.getId(), findWallet.getBalance());
    }

    @Override
    public BalanceChargeResponse charge(Long walletId, long chargeAmount) {

        Wallet target = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        Wallet charged = target.charge(chargeAmount);

        Wallet chargeResult = walletRepository.save(charged);

        return new BalanceChargeResponse(chargeResult.getId(), chargeResult.getBalance());
    }
}
