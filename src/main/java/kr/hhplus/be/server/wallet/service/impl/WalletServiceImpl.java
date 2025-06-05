package kr.hhplus.be.server.wallet.service.impl;

import kr.hhplus.be.server.wallet.domain.Wallet;
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
    public Wallet getBalance(Long walletId) {
        return walletRepository.findByUserId(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Override
    public Wallet charge(Long walletId, long chargeAmount) {
        Wallet target = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        Wallet charged = target.charge(chargeAmount);

        return walletRepository.save(charged);
    }
}
