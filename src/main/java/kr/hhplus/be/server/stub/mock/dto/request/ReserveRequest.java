package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 요청 DTO
 * @param seatId - 예약할 좌석 번호
 * @param reserveDate - 예약할 날짜
 */
public record ReserveRequest(
        @Schema(description = "좌석 번호", example = "1")
        String seatId,

        @Schema(description = "예약 날짜", example = "2025-12-31")
        String reserveDate
) {

}
