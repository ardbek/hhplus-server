package kr.hhplus.be.server.reservation.application.reservation;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationInfoJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;

public class GetAvailableSeatsUseCase {

    private final ReservationInfoJpaRepository reservationInfoJpaRepository;
    private final SeatJpaRepository seatJpaRepository;

    public GetAvailableSeatsUseCase(ReservationInfoJpaRepository reservationInfoJpaRepository,
        SeatJpaRepository seatJpaRepository) {
        this.reservationInfoJpaRepository = reservationInfoJpaRepository;
        this.seatJpaRepository = seatJpaRepository;
    }

    public List<SeatEntity> getAvailableSeats(Long scheduleId) {
        List<Long> reservedIds = reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, ReservationStatus.reservedStatuses());;
        return seatJpaRepository.findByConcertScheduleId(scheduleId).stream()
            .filter(seat -> !reservedIds.contains(seat.getId()))
            .toList();
    }
}
