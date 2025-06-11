package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationInfoJpaRepository extends JpaRepository<Reservation, Long> {

    /*예약된 좌석 조회*/
    @Query("SELECT r.seatEntity.id FROM Reservation r WHERE r.concertScheduleEntity.id = :scheduleId AND r.status IN :statuses")
    List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses);
}
