package kr.hhplus.be.server.queue.service;

import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;

public interface QueueTokenService {

    QueueTokenIssueResponse issueToken(Long userId);

    QueueStatusResponse checkStatus(String token);
}
