package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 가능 날짜 조회 요청 DTO
 * @param concertId - 조회할 콘서트 번호
 */
public record ReserveDateRequest(
        @Schema(description = "콘서트 번호", example = "1")
        String concertId
) {

}
