package kr.hhplus.be.server.reservation.interfaces.web;

import kr.hhplus.be.server.reservation.application.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public ReservationController(ReserveTemporarySeatUseCase reserveTemporarySeatUseCase,
            ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.reserveTemporarySeatUseCase = reserveTemporarySeatUseCase;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    /**
     * 임시 예약
     * @param request
     * @return
     */
    @PostMapping("/reserve-temporary")
    public Reservation reserveTemporary(@RequestBody ReserveRequest request) {
        return reserveTemporarySeatUseCase.reserveTemporary(request.userId(), request.concertScheduleId(), request.seatId());
    }

    /**
     * 예약 확정
     * @param request
     */
    @PostMapping("/reserve-confirm")
    public void confirmReservation(@RequestBody ConfirmRequest request) {
        confirmPaymentUseCase.confirmReservation(request.userId(), request.reservationId());
    }

    public static record ReserveRequest(Long userId, Long concertScheduleId, Long seatId) {}
    public static record ConfirmRequest(Long userId, Long reservationId) {}
}
