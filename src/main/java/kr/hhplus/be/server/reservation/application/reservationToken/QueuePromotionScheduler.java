package kr.hhplus.be.server.reservation.application.reservationToken;

import java.util.Set;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueuePromotionScheduler {

    private final ReservationTokenRepository tokenRepository;
    private static final int MAX_ACTIVE_USERS = 50;

    public QueuePromotionScheduler(ReservationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedDelay = 1000)
    public void promoteWaitingUsers() {
        long activeCount = tokenRepository.getActiveUserCount();
        long vacancies = MAX_ACTIVE_USERS - activeCount;

        if (vacancies <= 0) {
            return;
        }

        // 1. 빈 자리만큼 대기열에서 상태 변경
        Set<Long> promotionTargets = tokenRepository.getTopRankedUsers(vacancies);

        if(promotionTargets.isEmpty()) {
            return;
        }

        // 2. 대기열에서 제거, 처리 큐로 이동
        for (Long userId : promotionTargets) {
            tokenRepository.removeFromWaiting(userId);
            tokenRepository.addProcessingQueue(userId);
        }

        log.info(promotionTargets.size() + " 명 처리큐로 이동.");
    }
}
