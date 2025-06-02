package kr.hhplus.be.server.stub.mock.dto.response;

/**
 * 잔액 조회 응답 DTO
 *
 * @param userId  - 유저 번호
 * @param balance - 잔액
 */
public record BalanceResponse(
        String userId,
        long balance
) {

}
