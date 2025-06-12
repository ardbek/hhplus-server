package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.seatEntity.id = :seatId " +
            "AND r.concertScheduleEntity.id = :concertScheduleId " +
            "AND r.status = :status")
    Optional<ReservationEntity> findReservation(
            @Param("seatId") Long seatId,
            @Param("concertScheduleId") Long concertScheduleId,
            @Param("status") ReservationStatus status
    );

    /*예약된 좌석 조회*/
    @Query("SELECT r.seatEntity.id FROM ReservationEntity r WHERE r.concertScheduleEntity.id = :scheduleId AND r.status IN :statuses")
    List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses);

}
