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

    @InjectMocks
    QueueTokenServiceImpl queueTokenServiceImpl;

    @Test
    @DisplayName("대기열 토큰을 정상적으로 발급한다.")
    void issue_token_success() {
        // given
        Long userId = 1L;
        given(queueTokenRepository.existsByUserIdAndStatus(userId, TokenStatus.WAITING)).willReturn(
            false);
        given(queueTokenRepository.findMaxPosition()).willReturn(Optional.of(10));

        // when
        QueueTokenIssueResponse response = queueTokenServiceImpl.issueToken(userId);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.position()).isEqualTo(11);
        assertThat(response.status()).isEqualTo(TokenStatus.WAITING.name());
    }

    @Test
    @DisplayName("이미 대기열에 있는 유저가 토큰 발급을 시도할 경우 예외가 발생한다.")
    void issue_token_fail_duplicateUser() {
        // given
        Long userId = 1L;
        given(queueTokenRepository.existsByUserIdAndStatus(userId, TokenStatus.WAITING)).willReturn(
            true);

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
            .userId(1L)
            .token(token)
            .position(5)
            .status(TokenStatus.WAITING)
            .issuedAt(LocalDateTime.now())
            .build();

        given(queueTokenRepository.findByToken(token)).willReturn(Optional.of(queueToken));

        // when
        QueueStatusResponse response = queueTokenServiceImpl.checkStatus(token);

        // then
        assertThat(response.position()).isEqualTo(5);
        assertThat(response.status()).isEqualTo(TokenStatus.WAITING.name());

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
