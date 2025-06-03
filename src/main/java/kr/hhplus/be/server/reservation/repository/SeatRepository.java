package kr.hhplus.be.server.reservation.repository;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByConcertScheduleId(Long scheduleId);
}
