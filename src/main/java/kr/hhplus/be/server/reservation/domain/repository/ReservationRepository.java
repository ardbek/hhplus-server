package kr.hhplus.be.server.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    boolean existsLocked(Long seatId, Long concertScheduleId);

    List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses);
}
