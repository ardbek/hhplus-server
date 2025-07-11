package kr.hhplus.be.server.reservation.application.reservation.port.out;

import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;

public interface DataPlatformApiClient {

    /**
     * 예약 정보를 데이터 플랫폼에 전송.
     * @param event 예약 확정 정보 이벤트
     */
    void sendReservationData(ReservationConfirmedEvent event);

}
