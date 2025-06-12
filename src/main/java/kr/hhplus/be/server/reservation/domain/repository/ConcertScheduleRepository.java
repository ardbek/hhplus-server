package kr.hhplus.be.server.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;

public interface ConcertScheduleRepository {

    ConcertSchedule save(ConcertSchedule concertSchedule);

    Optional<ConcertSchedule> findById(Long id);

    List<ConcertSchedule> findByConcertId(Long concertId);
}