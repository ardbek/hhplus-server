package kr.hhplus.be.server.reservation.dto.response;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.Seat;

public record AvailableSeatsResponse(
    long scheduleId,
    List<Seat> availableSeats
) {

}
