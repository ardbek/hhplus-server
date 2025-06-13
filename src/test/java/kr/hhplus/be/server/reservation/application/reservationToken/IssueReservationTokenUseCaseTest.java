package kr.hhplus.be.server.reservation.application.reservationToken;

import kr.hhplus.be.server.queue.exception.AlreadyInQueueException;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class IssueReservationTokenUseCaseTest {

    @InjectMocks
    private IssueReservationTokenUseCase issueReservationTokenUseCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationTokenRepository reservationTokenRepository;

    @Test
    @DisplayName("성공 - 토큰 발급에 성공한다")
    void issueReservationToken_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reservationTokenRepository.isActiveUser(userId)).willReturn(false);
        given(reservationTokenRepository.getRank(userId)).willReturn(null);

        // when
        String token = issueReservationTokenUseCase.issueReservationToken(userId);

        // then
        assertThat(token).isNotNull();

        verify(reservationTokenRepository).saveToken(any(String.class), any(Long.class));
        verify(reservationTokenRepository).addWaiting(any(Long.class));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void issueReservationToken_fail_userNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            issueReservationTokenUseCase.issueReservationToken(userId);
        });
    }

    @Test
    @DisplayName("실패 - 이미 활성 상태인 사용자")
    void issueReservationToken_fail_alreadyActive() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reservationTokenRepository.isActiveUser(userId)).willReturn(true);

        // when & then
        assertThrows(AlreadyInQueueException.class, () -> {
            issueReservationTokenUseCase.issueReservationToken(userId);
        });
    }

    @Test
    @DisplayName("실패 - 이미 대기열에 있는 사용자")
    void issueReservationToken_fail_alreadyInWaitingQueue() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reservationTokenRepository.isActiveUser(userId)).willReturn(false);
        given(reservationTokenRepository.getRank(userId)).willReturn(10L); // 대기 순위가 있음

        // when & then
        assertThrows(AlreadyInQueueException.class, () -> {
            issueReservationTokenUseCase.issueReservationToken(userId);
        });
    }
}
