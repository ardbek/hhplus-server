package kr.hhplus.be.server.reservation.infrastructure.persistence.seat;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {

    List<SeatEntity> findByConcertScheduleEntity_Id(Long scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SeatEntity s where s.id = :seatId")
    @QueryHints(@QueryHint(name="javax.persistence.lock.timeout", value = "3000"))
    Optional<SeatEntity> findByIdForUpdate(Long seatId);
}
