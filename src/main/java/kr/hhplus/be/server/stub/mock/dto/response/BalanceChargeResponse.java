package kr.hhplus.be.server.stub.mock.dto.response;


/**
 * 잔액 충전 요청 DTO
 * @param userId - 유저 번호
 * @param balance - 충전 후 금액
 * @param chargeAmount - 충전 금액
 */
public record BalanceChargeResponse(
    String userId,
    long balance,
    long chargeAmount
) {

}
