package kr.hhplus.be.server.reservation.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    boolean existsLocked(Long seatId, Long concertScheduleId);

    List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses);

    List<Reservation> findReservationsToExpire(ReservationStatus status, LocalDateTime now);

    void updateStatusToCanceledByIds(List<Long> reservationIds, ReservationStatus status);

    /**
     * 특정 콘서트 회차 ID와 예약 상태에 해당하는 예약 건수 조회
     * @param scheduleId 콘서트 회차 Id
     * @param reservationStatus 예약 상태
     * @return 예약 건수
     */
    long countByConcertScheduleEntity_IdAndStatus(Long scheduleId, ReservationStatus reservationStatus);
}
