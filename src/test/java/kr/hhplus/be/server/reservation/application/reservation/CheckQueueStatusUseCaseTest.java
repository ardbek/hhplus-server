package kr.hhplus.be.server.reservation.application.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import kr.hhplus.be.server.reservation.domain.ReservationTokenStatus;
import kr.hhplus.be.server.reservation.exception.reservationToken.TokenNotFoundException;
import kr.hhplus.be.server.reservation.application.reservationToken.CheckQueueStatusUseCase;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.reservation.ReservationTokenStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckQueueStatusUseCaseTest {

    @InjectMocks
    private CheckQueueStatusUseCase checkQueueStatusUseCase;

    @Mock
    private ReservationTokenRepository tokenRepository;

    private String validToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        validToken = "test-token-123";
        userId = 1L;
    }

    @Test
    @DisplayName("사용자가 이미 활성 상태일 경우 ACTIVE를 반환한다")
    void checkStatus_withActiveUser_shouldReturnActiveStatus() {
        // given
        given(tokenRepository.getUserIdByToken(validToken)).willReturn(userId);
        given(tokenRepository.isActiveUser(userId)).willReturn(true);

        // when
        ReservationTokenStatusResponse response = checkQueueStatusUseCase.checkStatus(validToken);

        // then
        assertThat(response.status()).isEqualTo(ReservationTokenStatus.ACTIVE.name());
        assertThat(response.rank()).isZero();
    }

    @Test
    @DisplayName("사용자가 대기 상태일 경우 WAITING과 대기순번을 반환한다")
    void checkStatus_withWaitingUser_shouldReturnWaitingStatusAndRank() {
        // given
        Long rank = 10L; // 현재 대기 순번이 10번이라고 가정 (0부터 시작)
        given(tokenRepository.getUserIdByToken(validToken)).willReturn(userId);
        given(tokenRepository.isActiveUser(userId)).willReturn(false); // 활성 상태가 아님
        given(tokenRepository.getRank(userId)).willReturn(rank);

        // when
        ReservationTokenStatusResponse response = checkQueueStatusUseCase.checkStatus(validToken);

        // then
        assertThat(response.status()).isEqualTo(ReservationTokenStatus.WAITING.name());
        assertThat(response.rank()).isEqualTo(rank + 1); // 실제 사용자에게 보여지는 대기 번호
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 조회 시 예외를 반환한다")
    void checkStatus_withInvalidToken_shouldThrowException() {
        // given
        String invalidToken = "invalid-token";
        given(tokenRepository.getUserIdByToken(invalidToken)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> checkQueueStatusUseCase.checkStatus(invalidToken))
                .isInstanceOf(TokenNotFoundException.class);
    }

    @Test
    @DisplayName("토큰은 유효하지만 대기열에 없을 경우(rank=null) 예외를 반환한다")
    void checkStatus_withValidTokenButNoRank_shouldThrowException() {
        // given
        given(tokenRepository.getUserIdByToken(validToken)).willReturn(userId);
        given(tokenRepository.isActiveUser(userId)).willReturn(false);
        given(tokenRepository.getRank(userId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> checkQueueStatusUseCase.checkStatus(validToken))
                .isInstanceOf(TokenNotFoundException.class);
    }



}
