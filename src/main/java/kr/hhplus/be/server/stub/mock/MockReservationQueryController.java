package kr.hhplus.be.server.stub.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.hhplus.be.server.stub.mock.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.stub.mock.dto.response.AvailableSeatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/reservation")
@Tag(name = "Mock - 예약 가능 날짜/ 좌석 조회")
public class MockReservationQueryController {

    @Operation(summary = "예약 가능 날짜 조회", description = "콘서트에 해당하는 예약 가능 날짜 목록을 응답")
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<AvailableDatesResponse> getAvailableDates(@PathVariable @Schema(description = "콘서트 번호", example = "1") String concertId) {
        return ResponseEntity.ok(new AvailableDatesResponse(
                concertId,
                List.of("2025-07-01", "2025-07-02", "2025-07-03")
        ));
    }

    @Operation(summary = "예약 가능 좌석 조회", description = "예약 가능 날짜를 전달 받아 예약 가능한 좌석 조회")
    @GetMapping("/{concertId}/dates/{date}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(
            @PathVariable @Schema(description = "콘서트 번호", example = "1") String concertId,
            @PathVariable @Schema(description = "예약 날짜", example = "2025-09-01") String date
    ) {
        return ResponseEntity.ok(new AvailableSeatsResponse(
                concertId,
                date,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 50)
        ));
    }

}
