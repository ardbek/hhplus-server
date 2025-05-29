package kr.hhplus.be.server.stub.mock.dto.response;

/**
 * 토큰 발급 응답 DTO
 * @param holdingToken - 토큰
 * @param queuePosition - 대기 순번
 */
public record TokenIssueResponse(
        String holdingToken,
        long queuePosition
) {

}
