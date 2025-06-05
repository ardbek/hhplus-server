package kr.hhplus.be.server.queue.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;
import kr.hhplus.be.server.queue.exception.AlreadyInQueueException;
import kr.hhplus.be.server.queue.exception.TokenNotFoundException;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.queue.service.impl.QueueTokenServiceImpl;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueueTokenServiceTest {

    @Mock
    QueueTokenRepository queueTokenRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    QueueTokenServiceImpl queueTokenServiceImpl;

    @Test
    @DisplayName("대기열 토큰을 정상적으로 발급한다.")
    void issue_token_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(1L).build();
        given(queueTokenRepository.existsByUserAndStatus(user, TokenStatus.WAITING)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        QueueToken token = queueTokenServiceImpl.issueToken(userId);

        // then
        assertThat(token.getUser().getId()).isEqualTo(userId);
        assertThat(token.getStatus()).isEqualTo(TokenStatus.WAITING);
    }

    @Test
    @DisplayName("이미 대기열에 있는 유저가 토큰 발급을 시도할 경우 예외가 발생한다.")
    void issue_token_fail_duplicateUser() {
        // given
        Long userId = 1L;
        User user = User.builder().id(1L).build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(queueTokenRepository.existsByUserAndStatus(user, TokenStatus.WAITING)).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() -> queueTokenServiceImpl.issueToken(userId));

        // then
        assertThat(throwable).isInstanceOf(AlreadyInQueueException.class)
            .hasMessageContaining("이미 대기열에 있습니다.");

    }

    @Test
    @DisplayName("대기열 조회를 성공한다.")
    void checkStatus_success() {
        // given
        String token = UUID.randomUUID().toString();
        QueueToken queueToken = QueueToken.builder()
            .user(null)
            .token(token)
            .status(TokenStatus.WAITING)
            .issuedAt(LocalDateTime.now())
            .build();

        given(queueTokenRepository.findByToken(token)).willReturn(Optional.of(queueToken));

        given(queueTokenRepository.countByStatusAndCreatedAtBefore(
                TokenStatus.WAITING, queueToken.getIssuedAt()
        )).willReturn(6);

        // when
        QueueStatusResponse response = queueTokenServiceImpl.checkStatus(token);

        // then
        assertThat(response.position()).isEqualTo(7);
        assertThat(response.status()).isEqualTo(TokenStatus.WAITING.name());

    }

    @Test
    @DisplayName("내 순번이 입장 가능 구간이면 상태가 ACTIVE로 바뀐다.")
    void checkStatus_activeTransition() {
        // given
        String token = UUID.randomUUID().toString();
        QueueToken queueToken = QueueToken.builder()
                .user(null)
                .token(token)
                .status(TokenStatus.WAITING)
                .issuedAt(LocalDateTime.now())
                .build();

        given(queueTokenRepository.findByToken(token)).willReturn(Optional.of(queueToken));
        // position 3 (입장가능 범위)
        given(queueTokenRepository.countByStatusAndCreatedAtBefore(
                TokenStatus.WAITING, queueToken.getIssuedAt()
        )).willReturn(2);

        // when
        QueueStatusResponse response = queueTokenServiceImpl.checkStatus(token);

        // then
        assertThat(queueToken.getStatus()).isEqualTo(TokenStatus.ACTIVE); // 실제 객체 상태 검증
        assertThat(response.status()).isEqualTo(TokenStatus.ACTIVE.name()); // 응답 상태도 ACTIVE
        assertThat(response.position()).isEqualTo(3);
    }

    @Test
    @DisplayName("내 순번이 입장 가능 구간이 아니면 토큰 상태가 WAITING을 유지한다.")
    void checkStatus_statusIsStillWaiting() {
        // given
        String token = UUID.randomUUID().toString();
        QueueToken queueToken = QueueToken.builder()
                .user(null)
                .token(token)
                .status(TokenStatus.WAITING)
                .issuedAt(LocalDateTime.now())
                .build();

        given(queueTokenRepository.findByToken(token)).willReturn(Optional.of(queueToken));
        given(queueTokenRepository.countByStatusAndCreatedAtBefore(
                TokenStatus.WAITING, queueToken.getIssuedAt()
        )).willReturn(6);

        // when
        QueueStatusResponse response = queueTokenServiceImpl.checkStatus(token);

        // then
        assertThat(queueToken.getStatus()).isEqualTo(TokenStatus.WAITING);
        assertThat(response.status()).isEqualTo(TokenStatus.WAITING.name());
        assertThat(response.position()).isEqualTo(7);
    }


    @Test
    @DisplayName("유효하지 않은 토큰으로 조회 시 예외가 발생한다.")
    void checkStatus_fail_tokenNotFound() {
        // given
        String token = UUID.randomUUID().toString();
        given(queueTokenRepository.findByToken(token)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> queueTokenServiceImpl.checkStatus(token));

        // then
        assertThat(throwable).isInstanceOf(TokenNotFoundException.class)
            .hasMessageContaining("유효하지 않은 토큰입니다.");

    }

}
