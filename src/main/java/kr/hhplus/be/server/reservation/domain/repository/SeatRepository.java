package kr.hhplus.be.server.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.model.Seat.SeatStatus;

public interface SeatRepository {

    Seat save(Seat seat);

    Optional<Seat> findById(Long seatId);

    Optional<Seat> findByIdForUpdate(Long seatId);

    List<Seat> findByConcertScheduleId(Long scheduleId);

    void updateStatusToCanceledByIds(List<Long> seatIds, SeatStatus status);
}