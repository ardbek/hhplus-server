package kr.hhplus.be.server.reservation.application.reservation;

import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.exception.seat.SeatNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

public class ReserveTemporarySeatUseCase {
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ReserveTemporarySeatUseCase(ReservationRepository reservationRepository,
            SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    @CacheEvict(value = "availableSeats", key = "#concertScheduleId")
    public Reservation reserveTemporary(Long userId, Long concertScheduleId, Long seatId) {
        // 1. 비관적 락으로 좌석 조회
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(SeatNotFoundException::new);

        // 2. 좌석 상태 확인 및 변경
        if (seat.isReserved()) {
            throw new SeatAlreadyReservedException();
        }

        // 3. 좌석 상태 변경 및 저장
        seat.reserve();
        seatRepository.save(seat);

        // 4. 예약 생성
        Reservation reservation = Reservation.reserveTemporary(userId, seatId, concertScheduleId,
                reservationRepository.existsLocked(seatId, concertScheduleId));

        return reservationRepository.save(reservation);
    }
}
