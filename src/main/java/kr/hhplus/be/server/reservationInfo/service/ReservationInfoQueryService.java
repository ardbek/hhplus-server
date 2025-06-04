package kr.hhplus.be.server.reservationInfo.service;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservationInfo.dto.response.AvailableSeatsResponse;

public interface ReservationInfoQueryService {

    List<LocalDate> getAvailableDates(Long concertId);

    List<Seat> getAvailableSeats(Long scheduleId);
}
