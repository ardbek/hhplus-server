package kr.hhplus.be.server.reservation.application;

import kr.hhplus.be.server.reservation.application.reservationToken.QueueActivationScheduler;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class QueueActivationSchedulerTest {

    @InjectMocks
    private QueueActivationScheduler queueActivationScheduler;

    @Mock
    private ReservationTokenRepository reservationTokenRepository;

    @Test
    @DisplayName("성공 - 대기열에 사용자가 있어 활성화 작업을 수행한다")
    void activateWaitingUsers_success() {
        // given
        // 버그 수정 전 코드: if (!activationTargets.isEmpty()) return;
        // 버그 수정 후 코드: if (activationTargets.isEmpty()) return;
        // 아래 테스트는 수정 후 코드 기준으로 작성되었습니다.

        Set<Long> usersToActivate = Set.of(1L, 2L, 3L);
        given(reservationTokenRepository.getTopRankedUsers(50)).willReturn(usersToActivate);

        // when
        queueActivationScheduler.activateWaitingUsers();

        // then
        // 3명의 유저에 대해 각각 1번씩 호출되었는지 검증
        verify(reservationTokenRepository, times(3)).setActiveUser(anyLong());
        verify(reservationTokenRepository, times(3)).removeFromWaiting(anyLong());
    }

    @Test
    @DisplayName("성공 - 대기열이 비어있어 아무 작업도 수행하지 않는다")
    void activateWaitingUsers_doNothing_whenQueueIsEmpty() {
        // given
        given(reservationTokenRepository.getTopRankedUsers(50)).willReturn(Set.of());

        // when
        queueActivationScheduler.activateWaitingUsers();

        // then
        // 활성화 및 제거 로직이 전혀 호출되지 않았는지 검증
        verify(reservationTokenRepository, never()).setActiveUser(anyLong());
        verify(reservationTokenRepository, never()).removeFromWaiting(anyLong());
    }
}