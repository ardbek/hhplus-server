package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.event.listener;

import kr.hhplus.be.server.reservation.application.reservation.RecordSoldOutUseCase;
import kr.hhplus.be.server.reservation.domain.event.ConcertSoldOutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * ConfirmPaymentUseCase.ConcertSoldOutEvent가 발행되면 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoldOutCheckListener {

    private final RecordSoldOutUseCase recordSoldOutUseCase;

    @Async
    @TransactionalEventListener
    public void handleSoldOutEvent(ConcertSoldOutEvent event) {
        log.info("매진 이벤트 수신 완료. 랭킹 기록 시작 concertId={}, scheduleId={}", event.concertId(), event.concertScheduleId());

        recordSoldOutUseCase.record(
                event.concertId(),
                event.concertScheduleId()
        );
    }

}
