package kr.hhplus.be.server.reservationInfo.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableDatesResponse(
    long concertId,
    List<LocalDate> availableDates
) {

}
