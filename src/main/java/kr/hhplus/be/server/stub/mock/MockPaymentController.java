package kr.hhplus.be.server.stub.mock;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.stub.mock.dto.request.MockPaymentRequest;
import kr.hhplus.be.server.stub.mock.dto.response.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/payments")
@Tag(name = "Mock - 결제 API")
public class MockPaymentController {

    @Operation(summary = "결제 처리", description = "결제를 처리하고 좌석을 확정하며 대기열 토큰을 만료시킵니다.")
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@RequestBody MockPaymentRequest request) {
        return ResponseEntity.ok(
                new PaymentResponse("1", request.userId(), request.seatId(), "SUCCESS", true));
    }
}
