package kr.hhplus.be.server.reservation.service.impl;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservation.domain.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.Seat;
import kr.hhplus.be.server.reservation.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservation.dto.response.AvailableSeatsResponse;
import kr.hhplus.be.server.reservation.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.repository.SeatRepository;
import kr.hhplus.be.server.reservation.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl implements ReservationQueryService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 예약 가능 날짜 조회
     * @param concertId
     * @return
     */
    @Override
    public AvailableDatesResponse getAvailableDates(Long concertId) {
        List<LocalDate> availableDates = concertScheduleRepository.findByConcertId(concertId).stream()
            .map(schedule -> schedule.getStartAt().toLocalDate())
            .distinct()
            .sorted()
            .toList();

        return new AvailableDatesResponse(concertId, availableDates);
    }

    /**
     * 예약 가능 좌석 조회
     * @param scheduleId
     * @return
     */
    @Override
    public AvailableSeatsResponse getAvailableSeats(Long scheduleId) {
        List<Long> reservedIds = reservationRepository.findByReservedSeatIds(scheduleId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED));;
        List<Seat> available = seatRepository.findByConcertScheduleId(scheduleId).stream()
            .filter(seat -> !reservedIds.contains(seat.getId()))
            .toList();
        return new AvailableSeatsResponse(scheduleId, available);
    }
}
