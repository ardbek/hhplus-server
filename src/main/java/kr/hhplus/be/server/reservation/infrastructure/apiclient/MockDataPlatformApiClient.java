package kr.hhplus.be.server.reservation.infrastructure.apiclient;

import kr.hhplus.be.server.reservation.application.reservation.port.out.DataPlatformApiClient;
import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockDataPlatformApiClient implements DataPlatformApiClient {

    @Override
    public void sendReservationData(ReservationConfirmedEvent event) {
        log.info("[Mock API] 데이터 플랫폼으로 예약 정보 전송");
        log.info("[Mock API] 전송 데이터: {}", event.toString());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Mock API] 시뮬레이션 지연 중 오류 발생", e);
        }

        if (event.reservationId() % 10 == 0) {
            log.warn("[Mock API] 의도된 실패 시뮬레이션 발생: reservationId={}", event.reservationId());
             throw new RuntimeException("Mock API call failed intentionally.");
        }

        log.info("[Mock API] 예약 정보 전송 성공");

    }
}
