package kr.hhplus.be.server.reservation.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.model.Seat.SeatStatus;

public interface SeatRepository {

    Seat save(Seat seat);

    Optional<Seat> findById(Long seatId);

    Optional<Seat> findByIdForUpdate(Long seatId);

    List<Seat> findByConcertScheduleId(Long scheduleId);

    void updateStatusToCanceledByIds(List<Long> seatIds, SeatStatus status);

    /**
     * 특정 콘서트 회차 ID에 해당하는 좌석의 총 개수 조회
     * @param scheduleId 콘서트 회차 ID
     * @return 좌석 수
     */
    long countByConcertScheduleEntity_Id(Long scheduleId);
}