package kr.hhplus.be.server.reservation.application.reservation;

import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.seat.SeatNotFoundException;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
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
    public Reservation reserveTemporary(Long userId, Long concertScheduleId, Long seatId) {

        // 비관적 락으로 동시성 제어
        seatRepository.findByIdForUpdate(seatId).orElseThrow(SeatNotFoundException::new);

        Reservation reservation = Reservation.reserveTemporary(userId, seatId, concertScheduleId,
                reservationRepository.existsLocked(seatId, concertScheduleId));

        return reservationRepository.save(reservation);
    }
}
