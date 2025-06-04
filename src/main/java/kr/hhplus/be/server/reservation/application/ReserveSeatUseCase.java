package kr.hhplus.be.server.reservation.application;

import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;


public class ReserveSeatUseCase {
    private final ReservationRepository reservationRepository;

    public ReserveSeatUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation reserve(Long userId, Long concertScheduleId, Long seatId) {
        if (reservationRepository.existsLocked(seatId, concertScheduleId)) {
            throw new IllegalStateException("이미 임시 예약된 좌석입니다.");
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
