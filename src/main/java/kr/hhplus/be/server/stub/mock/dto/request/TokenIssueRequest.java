package kr.hhplus.be.server.stub.mock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 발급 요청 DTO
 * @param userId - 유저 번호
 */
public record TokenIssueRequest(
        @Schema(description = "유저 번호", example = "1")
        String userId
) {
}
