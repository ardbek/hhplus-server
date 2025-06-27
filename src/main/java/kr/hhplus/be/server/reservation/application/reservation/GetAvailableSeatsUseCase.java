package kr.hhplus.be.server.reservation.application.reservation;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public class GetAvailableSeatsUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatJpaRepository seatJpaRepository;

    public GetAvailableSeatsUseCase(ReservationRepository reservationRepository,
        SeatJpaRepository seatJpaRepository) {
        this.reservationRepository = reservationRepository;
        this.seatJpaRepository = seatJpaRepository;
    }

    @Cacheable(value = "availableSeats", key = "#scheduleId", sync = true)
    public List<SeatEntity> getAvailableSeats(Long scheduleId) {
        List<Long> reservedIds = reservationRepository.findByReservedSeatIds(scheduleId, ReservationStatus.reservedStatuses());;
        return seatJpaRepository.findByConcertScheduleEntity_Id(scheduleId).stream()
            .filter(seat -> !reservedIds.contains(seat.getId()))
            .toList();
    }
}
