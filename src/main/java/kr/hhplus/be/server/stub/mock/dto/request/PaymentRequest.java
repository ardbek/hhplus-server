package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 결제 요청 DTO
 * @param userId - 유저 번호
 * @param seatId - 좌석 번호
 * @param token - 대기열 토큰
 */
public record PaymentRequest(
        @Schema(description = "유저 번호", example = "1")
        String userId,

        @Schema(description = "예약된 좌석 번호", example = "10")
        String seatId,

        @Schema(description = "대기열 토큰", example = "mock-token-12345678")
        String token
) {}
