package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 잔액 충전 요청 DTO
 * @param userId - 유저 번호
 * @param chargeAmount - 충전 금액
 */
public record MockBalanceChargeRequest(
    @Schema(description = "유저 번호", example = "1")
    String userId,

    @Schema(description = "충전 금액", example = "50000")
    long chargeAmount
) {

}
