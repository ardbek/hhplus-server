package kr.hhplus.be.server.reservation.application.reservationToken;

import jakarta.annotation.PreDestroy;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueActivationWorker{

    private final ReservationTokenRepository tokenRepository;

    @Async
    public void startProcessing() {
        log.info("대기열 활성화 워커 시작");

        while(true) {
            try{
                // 1. 처리 큐에서 작업 가져옴 (없는 경우 대기)
                Long userId = tokenRepository.getFromProcessingQueue();
                // 2. 사용자 상태 활성화
                tokenRepository.setActiveUser(userId);
                log.info("{} 상태 활성화로 변경", userId);
            } catch (InterruptedException e) {
                log.info("상태 활성화 워커 중단.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("활성화 작업 처리 중 오류 발생", e.getMessage());
            }
        }
    }

}
