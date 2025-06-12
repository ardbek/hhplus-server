package kr.hhplus.be.server.reservation.application.reservation;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;

public class GetAvailableSeatsUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatJpaRepository seatJpaRepository;

    public GetAvailableSeatsUseCase(ReservationRepository reservationRepository,
        SeatJpaRepository seatJpaRepository) {
        this.reservationRepository = reservationRepository;
        this.seatJpaRepository = seatJpaRepository;
    }

    public List<SeatEntity> getAvailableSeats(Long scheduleId) {
        List<Long> reservedIds = reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, ReservationStatus.reservedStatuses());;
        return seatJpaRepository.findByConcertScheduleId(scheduleId).stream()
            .filter(seat -> !reservedIds.contains(seat.getId()))
            .toList();
    }
}
