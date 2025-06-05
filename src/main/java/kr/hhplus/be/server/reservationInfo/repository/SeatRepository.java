package kr.hhplus.be.server.reservationInfo.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByConcertScheduleId(Long scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :seatId")
    @QueryHints(@QueryHint(name="javax.persistence.lock.timeout", value = "3000"))
    Optional<Seat> findByIdForUpdate(Long seatId);
}
