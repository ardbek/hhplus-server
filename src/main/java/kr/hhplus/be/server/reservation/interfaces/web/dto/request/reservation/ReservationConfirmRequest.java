package kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation;

public record ReservationConfirmRequest(
        Long userId, Long reservationId
) {

}
