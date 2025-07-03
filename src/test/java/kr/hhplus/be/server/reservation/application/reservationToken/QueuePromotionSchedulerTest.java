package kr.hhplus.be.server.reservation.application.reservationToken;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Set;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class QueuePromotionSchedulerTest {

    @Mock
    private ReservationTokenRepository tokenRepository;

    @InjectMocks
    private QueuePromotionScheduler scheduler;

    @Test
    void 활성_사용자가_30명일_때_20개의_빈자리를_채운다() {
        // given
        long activeCount = 30;
        long vacancies = 20;
        Set<Long> promotionTargets = Set.of(1L, 2L, 3L);

        given(tokenRepository.getActiveUserCount()).willReturn(activeCount);
        given(tokenRepository.getTopRankedUsers(vacancies)).willReturn(promotionTargets);

        // when
        scheduler.promoteWaitingUsers();

        // then
        verify(tokenRepository).getTopRankedUsers(vacancies);
        verify(tokenRepository).removeFromWaiting(1L);
        verify(tokenRepository).addProcessingQueue(2L);

    }

}