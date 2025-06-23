package kr.hhplus.be.server.reservation.application.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat.SeatStatus;
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
        List<Reservation> expiredTargetReservations  = reservationRepository.findReservationsToExpire(
                ReservationStatus.LOCKED, LocalDateTime.now().minusMinutes(5));

        if(expiredTargetReservations .isEmpty()) {
            log.info("만료된 임시 예약 없음");
            return;
        }

        // 2. 만료시킬 예약과 좌석의 ID 목록 추출
        List<Long> reservationIdsToExpire = expiredTargetReservations.stream()
                .map(Reservation::getId)
                .collect(Collectors.toList());

        List<Long> seatIdsToMakeAvailable = expiredTargetReservations.stream()
                .map(Reservation::getSeatId)
                .collect(Collectors.toList());

        log.info("{}개의 임시 예약을 만료 처리합니다. 대상 예약 ID: {}", reservationIdsToExpire.size(), reservationIdsToExpire);

        // 예약 가능 상태로 변경
        reservationRepository.updateStatusToCanceledByIds(reservationIdsToExpire, ReservationStatus.RELEASED);
        seatRepository.updateStatusToCanceledByIds(seatIdsToMakeAvailable, SeatStatus.AVAILABLE);
    }
}
