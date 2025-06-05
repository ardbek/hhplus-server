package kr.hhplus.be.server.queue.dto.response;

import java.time.LocalDateTime;

public record QueueTokenIssueResponse(
    long userId,
    String token,
    String status,
    LocalDateTime issuedAt,
    LocalDateTime expiresAt

) {

}
