package kr.hhplus.be.server.reservationInfo.repository;

import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationInfoRepository extends JpaRepository<Reservation, Long> {

    /*예약된 좌석 조회*/
    @Query("SELECT r.seat.id FROM Reservation r WHERE r.concertSchedule.id = :scheduleId AND r.status IN :statuses")
    List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses);
}
