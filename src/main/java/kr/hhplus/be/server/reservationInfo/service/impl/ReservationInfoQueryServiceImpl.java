package kr.hhplus.be.server.reservationInfo.service.impl;

import static kr.hhplus.be.server.reservation.domain.ReservationStatus.reservedStatuses;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservationInfo.repository.ReservationInfoRepository;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.reservationInfo.service.ReservationInfoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationInfoQueryServiceImpl implements ReservationInfoQueryService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;
    private final ReservationInfoRepository reservationInfoRepository;

    /**
     * 예약 가능 날짜 조회
     * @param concertId
     * @return
     */
    @Override
    public List<LocalDate> getAvailableDates(Long concertId) {
        return concertScheduleRepository.findByConcertId(concertId).stream()
                .map(schedule -> schedule.getStartAt().toLocalDate())
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * 예약 가능 좌석 조회
     * @param scheduleId
     * @return
     */
    @Override
    public List<Seat> getAvailableSeats(Long scheduleId) {
        List<Long> reservedIds = reservationInfoRepository.findByReservedSeatIds(scheduleId, reservedStatuses());;
        return seatRepository.findByConcertScheduleId(scheduleId).stream()
                .filter(seat -> !reservedIds.contains(seat.getId()))
                .toList();
    }
}
