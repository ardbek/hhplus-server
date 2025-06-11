package kr.hhplus.be.server.reservation.application.reservationToken;

import java.util.Set;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;

public class QueueActivationScheduler {

    private final ReservationTokenRepository tokenRepository;
    private static final int ACTIVE_USER_COUNT = 50;

    public QueueActivationScheduler(ReservationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedDelay = 1000)
    public void activateWaitingUsers() {
        Set<Long> activationTargets = tokenRepository.getTopRankedUsers(ACTIVE_USER_COUNT);

        if (activationTargets.isEmpty()) {
            return;
        }

        for (Long userId : activationTargets) {
            tokenRepository.setActiveUser(userId);
            tokenRepository.removeFromWaiting(userId);
        }
        System.out.println(activationTargets.size() + "명의 유저가 활성화되었습니다.");
    }
}
