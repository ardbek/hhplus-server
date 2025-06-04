package kr.hhplus.be.server.reservation.interfaces.web;

import kr.hhplus.be.server.reservation.application.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.ReserveSeatUseCase;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReserveSeatUseCase reserveSeatUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public ReservationController(ReserveSeatUseCase reserveSeatUseCase,
            ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.reserveSeatUseCase = reserveSeatUseCase;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    @PostMapping("/reserve")
    public Reservation reserve(@RequestBody ReserveRequest req) {
        return reserveSeatUseCase.reserve(req.userId(), req.concertScheduleId(), req.seatId());
    }

    @PostMapping("/confirm")
    public void confirm(@RequestBody ConfirmRequest req) {
        confirmPaymentUseCase.confirm(req.userId(), req.reservationId());
    }

    public static record ReserveRequest(Long userId, Long concertScheduleId, Long seatId) {}
    public static record ConfirmRequest(Long userId, Long reservationId) {}
}
