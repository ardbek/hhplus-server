package kr.hhplus.be.server.reservation.interfaces.web.controller;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservation.application.reservation.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableDatesUseCase;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableSeatsUseCase;
import kr.hhplus.be.server.reservation.application.reservation.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.AvailableSeatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {


    private final GetAvailableSeatsUseCase getAvailableSeatsUseCase;
    private final GetAvailableDatesUseCase getAvailableDatesUseCase;

    private final ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public ReservationController(GetAvailableSeatsUseCase getAvailableSeatsUseCase,
        GetAvailableDatesUseCase getAvailableDatesUseCase, ReserveTemporarySeatUseCase reserveTemporarySeatUseCase,
            ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.getAvailableSeatsUseCase = getAvailableSeatsUseCase;
        this.getAvailableDatesUseCase = getAvailableDatesUseCase;
        this.reserveTemporarySeatUseCase = reserveTemporarySeatUseCase;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    // 예약 가능 날짜 조회
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<AvailableDatesResponse> getAvailableDates(@PathVariable Long concertId) {
        List<LocalDate> availableDates = getAvailableDatesUseCase.getAvailableDates(concertId);
        return ResponseEntity.ok(new AvailableDatesResponse(concertId, availableDates));
    }


    // 예약 가능 좌석 조회
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(@PathVariable Long scheduleId) {
        List<SeatEntity> availableSeatEntities = getAvailableSeatsUseCase.getAvailableSeats(scheduleId);
        return ResponseEntity.ok(new AvailableSeatsResponse(scheduleId, availableSeatEntities));
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
