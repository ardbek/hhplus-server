package kr.hhplus.be.server.wallet.controller;

import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.wallet.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.wallet.dto.response.WalletBalanceResponse;
import kr.hhplus.be.server.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletBalanceResponse> getBalance(@RequestParam Long userId) {
        Wallet findWallet = walletService.getBalance(userId);
        return ResponseEntity.ok(new WalletBalanceResponse(findWallet.getId(), findWallet.getBalance()));
    }

    @PostMapping
    public ResponseEntity<BalanceChargeResponse> charge(@RequestBody BalanceChargeRequest request) {
        Wallet charged = walletService.charge(request.walletId(), request.chargeAmount());
        return ResponseEntity.ok(new BalanceChargeResponse(charged.getId(), charged.getBalance()));
    }
}
