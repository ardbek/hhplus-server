package kr.hhplus.be.server.reservation.interfaces.web.dto.response;

import java.util.List;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;

public record AvailableSeatsResponse(
    long scheduleId,
    List<SeatEntity> availableSeatEntities
) {

}
