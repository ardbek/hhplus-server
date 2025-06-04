package kr.hhplus.be.server.reservationInfo.repository;

import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {

    List<ConcertSchedule> findByConcertId(Long concertId);
}
