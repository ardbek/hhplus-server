package kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation;

public record ReserveConfirmRequest(
        Long userId, Long reservationId, Long seatId
) {

}
