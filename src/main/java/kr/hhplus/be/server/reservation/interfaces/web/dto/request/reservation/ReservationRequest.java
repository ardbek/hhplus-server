package kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation;

public record ReservationRequest(
        Long userId, Long seatId, Long concertScheduleId
) {
}
