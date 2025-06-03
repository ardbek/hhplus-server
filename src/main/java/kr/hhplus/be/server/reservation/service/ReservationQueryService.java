package kr.hhplus.be.server.reservation.service;

import kr.hhplus.be.server.reservation.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservation.dto.response.AvailableSeatsResponse;

public interface ReservationQueryService {

    AvailableDatesResponse getAvailableDates(Long concertId);

    AvailableSeatsResponse getAvailableSeats(Long scheduleId);
}
