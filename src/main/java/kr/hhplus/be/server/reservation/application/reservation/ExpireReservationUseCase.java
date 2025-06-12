package kr.hhplus.be.server.reservation.application.reservation;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class ExpireReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ExpireReservationUseCase(ReservationRepository reservationRepository,
            SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public void expireReservations() {
        List<Reservation> expiredReservations = reservationRepository.findReservationsToExpire(ReservationStatus.LOCKED, LocalDateTime.now());

        if(expiredReservations.isEmpty()) {
            log.info("만료된 임시 예약 없음");
            return;
        }

        log.info("{}개의 임시 예약 ", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            // 예약 상태 변경
            reservation.release();
            // 좌석 예약 가능으로 변경
            seatRepository.findById(reservation.getSeatId()).ifPresent(seat -> {
                seat.makeAvailable();
                seatRepository.save(seat); // 좌석 상태 업데이트 저장
            });

            reservationRepository.save(reservation); // 예약 상태 업데이트 저장
        }

    }
}
