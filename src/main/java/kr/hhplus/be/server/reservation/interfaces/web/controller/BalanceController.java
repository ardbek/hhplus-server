package kr.hhplus.be.server.reservation.interfaces.web.controller;

import kr.hhplus.be.server.reservation.application.balance.ChargeBalanceUseCase;
import kr.hhplus.be.server.reservation.application.balance.GetBalanceUseCase;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.interfaces.web.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.WalletBalanceResponse;
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
public class BalanceController {

    private final GetBalanceUseCase getBalanceUseCase;
    private final ChargeBalanceUseCase chargeBalanceUseCase;

    @GetMapping
    public ResponseEntity<WalletBalanceResponse> getBalance(@RequestParam Long userId) {
        Balance balance = getBalanceUseCase.getBalance(userId);
        return ResponseEntity.ok(new WalletBalanceResponse(balance.getId(), balance.getBalance()));
    }

    @PostMapping
    public ResponseEntity<BalanceChargeResponse> charge(@RequestBody BalanceChargeRequest request) {
        Balance charged = chargeBalanceUseCase.charge(request.walletId(), request.chargeAmount());
        return ResponseEntity.ok(new BalanceChargeResponse(charged.getId(), charged.getBalance()));
    }
}
