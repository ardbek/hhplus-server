package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.event.listener;

import kr.hhplus.be.server.reservation.application.reservation.port.out.DataPlatformApiClient;
import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final DataPlatformApiClient dataPlatformApiClient;

    @Async
    @TransactionalEventListener
    public void handleReservationConfirmedEvent(ReservationConfirmedEvent event) {
        log.info(
                "ReservationEventListener.handleReservationConfirmedEvent :: 예약 확정 이벤트 수신, reservationId={}",
                event.reservationId());
        try {
            dataPlatformApiClient.sendReservationData(event);
            log.info("데이터 플랫폼 전송 성공: reservationId={}", event.reservationId());
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패: reservationId={}, error={}", event.reservationId(), e.getMessage());
        }

    }
}
