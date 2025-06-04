package kr.hhplus.be.server.reservationInfo.dto.response;

import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Seat;

public record AvailableSeatsResponse(
    long scheduleId,
    List<Seat> availableSeats
) {

}
