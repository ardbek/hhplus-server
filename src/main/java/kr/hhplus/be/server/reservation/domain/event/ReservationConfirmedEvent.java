package kr.hhplus.be.server.reservation.domain.event;

/**
 * 예약 정보 전송을 위한 이벤트
 */
public record ReservationConfirmedEvent(
        Long reservationId,
        Long userId,
        Long seatId,
        Long price
) {

}
