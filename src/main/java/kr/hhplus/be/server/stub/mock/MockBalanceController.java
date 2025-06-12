package kr.hhplus.be.server.stub.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.stub.mock.dto.request.MockBalanceChargeRequest;
import kr.hhplus.be.server.stub.mock.dto.response.BalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/balance")
@Tag(name = "Mock - 잔액 충전 / 조회")
public class MockBalanceController {

    @Operation(summary = "잔액 조회", description = "잔액 조회")
    @GetMapping
    public ResponseEntity<BalanceResponse> getBalance(@RequestParam @Schema(description = "유저 번호", example = "1") String userId) {
        return ResponseEntity.ok(new BalanceResponse(userId, 50_000));
    }

    @Operation(summary = "잔액 충전", description = "잔액 충전")
    @PostMapping
    public ResponseEntity<BalanceResponse> charge(@RequestBody MockBalanceChargeRequest request) {
        return ResponseEntity.ok(new BalanceResponse(request.userId(), request.chargeAmount()));
    }

}
