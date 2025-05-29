package kr.hhplus.be.server.stub.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.stub.mock.dto.request.ReserveRequest;
import kr.hhplus.be.server.stub.mock.dto.response.ReserveResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/reservations")
@Tag(name="Mock - 예약")
public class MockSeatReservationController {

    @Operation(summary = "좌석 예약 요청", description = "날짜와 좌석 정보를 입력받아 예약을 수행합니다.")
    @PostMapping
    public ResponseEntity<ReserveResponse> reserve(@RequestBody ReserveRequest request) {
        return ResponseEntity.ok(new ReserveResponse(
                true,
                "2025-05-01T12:00:00",
                request.seatId(),
                "2025-07-01T12:00:00"
        ));
    }
}
