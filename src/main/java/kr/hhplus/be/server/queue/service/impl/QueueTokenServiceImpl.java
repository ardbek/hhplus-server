package kr.hhplus.be.server.queue.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;
import kr.hhplus.be.server.queue.exception.AlreadyInQueueException;
import kr.hhplus.be.server.queue.exception.TokenNotFoundException;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.queue.service.QueueTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueTokenServiceImpl implements QueueTokenService {

    private final QueueTokenRepository queueTokenRepository;

    @Override
    public QueueTokenIssueResponse issueToken(Long userId) {

        if(queueTokenRepository.existsByUserIdAndStatus(userId, TokenStatus.WAITING)) {
            throw new AlreadyInQueueException();
        }

        int newPosition = queueTokenRepository.findMaxPosition().orElse(0) + 1;

        QueueToken token = QueueToken.builder()
            .userId(userId)
            .token(UUID.randomUUID().toString())
            .position(newPosition)
            .status(TokenStatus.WAITING)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .build();

        queueTokenRepository.save(token);

        return new QueueTokenIssueResponse(token.getUserId(),token.getToken(), token.getPosition(), token.getStatus().name(),token.getIssuedAt(),token.getExpiresAt());
    }

    @Override
    public QueueStatusResponse checkStatus(String token) {
        QueueToken queueToken = queueTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenNotFoundException());

        return new QueueStatusResponse(queueToken.getPosition(), queueToken.getStatus().name());

    }
}
