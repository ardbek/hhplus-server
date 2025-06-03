package kr.hhplus.be.server.wallet.controller;

import kr.hhplus.be.server.wallet.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.wallet.dto.request.WalletBalanceRequest;
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
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    @PostMapping
    public ResponseEntity<BalanceChargeResponse> charge(@RequestBody BalanceChargeRequest request) {
        return ResponseEntity.ok(walletService.charge(request.walletId(), request.chargeAmount()));
    }
}
