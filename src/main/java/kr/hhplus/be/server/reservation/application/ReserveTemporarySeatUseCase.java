package kr.hhplus.be.server.reservation.application;

import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.exception.SeatNotFoundException;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
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

        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatNotFoundException());

        if (reservationRepository.existsLocked(seatId, concertScheduleId)) {
            throw new SeatAlreadyReservedException();
        }

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .concertScheduleId(concertScheduleId)
                .seatId(seatId)
                .status(ReservationStatus.LOCKED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation);
    }
}
