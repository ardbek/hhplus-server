package kr.hhplus.be.server.reservation.interfaces.web.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableDatesResponse(
    long concertId,
    List<LocalDate> availableDates
) {

}
