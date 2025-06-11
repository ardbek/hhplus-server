package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {
    Optional<ReservationEntity> findBySeatIdAndConcertScheduleIdAndStatus(Long seatId, Long concertScheduleId, ReservationStatus status);
}
