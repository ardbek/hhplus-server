package kr.hhplus.be.server.reservation.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private ExpireReservationUseCase expireReservationUseCase;

    public ReservationScheduler(ExpireReservationUseCase expireReservationUseCase) {
        this.expireReservationUseCase = expireReservationUseCase;
    }

    // 1분마다 만료된 임시 예약 정리
    @Scheduled(fixedRate = 60_000)
    public void expireTemporaryReservations(){
        expireReservationUseCase.expireReservations();
    }
}
