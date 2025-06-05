package kr.hhplus.be.server.queue.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.exception.AlreadyInQueueException;
import kr.hhplus.be.server.queue.exception.TokenNotFoundException;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.queue.service.QueueTokenService;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueTokenServiceImpl implements QueueTokenService {

    private final QueueTokenRepository queueTokenRepository;
    private final UserRepository userRepository;

    @Override
    public QueueToken issueToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if(queueTokenRepository.existsByUserAndStatus(user, TokenStatus.WAITING)) {
            throw new AlreadyInQueueException();
        }

        QueueToken token = QueueToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .status(TokenStatus.WAITING)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .build();

        queueTokenRepository.save(token);

        return token;
    }

    @Override
    public QueueStatusResponse checkStatus(String token) {
        QueueToken queueToken = queueTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenNotFoundException());

        int batchSize = 5;
        int position = getPosition(queueToken);

        if (position >= 1 && position <= batchSize
                && queueToken.getStatus() == TokenStatus.WAITING) {
            queueToken.active();
            queueTokenRepository.save(queueToken);
        }

        return new QueueStatusResponse(position, queueToken.getStatus().name());
    }

    private int getPosition(QueueToken token) {
        return queueTokenRepository.countByStatusAndCreatedAtBefore(
                TokenStatus.WAITING, token.getIssuedAt()
        ) + 1;
    }
}
