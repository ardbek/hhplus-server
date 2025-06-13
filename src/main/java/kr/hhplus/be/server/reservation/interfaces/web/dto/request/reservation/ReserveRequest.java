package kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation;

public record ReserveRequest(
        Long userId, Long concertScheduleId, Long seatId
) {

}
