package kr.hhplus.be.server.queue.service;

import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;

public interface QueueTokenService {

    QueueToken issueToken(Long userId);

    QueueStatusResponse checkStatus(String token);
}
