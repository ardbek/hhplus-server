package kr.hhplus.be.server.reservation.repository;

import java.util.List;
import kr.hhplus.be.server.reservation.domain.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    List<ConcertSchedule> findByConcertId(Long concertId);
}
