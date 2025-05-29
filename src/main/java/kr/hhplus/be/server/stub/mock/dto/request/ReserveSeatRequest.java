package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 가능 좌석 조회 요청 DTO
 * @param concertId - 조회할 콘서트 번호
 * @param date - 조회할 날짜
 */
public record ReserveSeatRequest(
        @Schema(description = "콘서트 번호", example = "1")
        String concertId,

        @Schema(description = "조회 날짜", example = "2025-07-01")
        String date
) {

}
