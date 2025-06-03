package kr.hhplus.be.server.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservation.domain.Seat;
import kr.hhplus.be.server.reservation.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservation.dto.response.AvailableSeatsResponse;
import kr.hhplus.be.server.reservation.service.ReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private ReservationQueryService reservationQueryService;

    // 예약 가능 날짜 조회
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<AvailableDatesResponse> getAvailableDates(@PathVariable Long concertId) {
        return ResponseEntity.ok(reservationQueryService.getAvailableDates(concertId));
    }


    // 예약 가능 좌석 조회
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(reservationQueryService.getAvailableSeats(scheduleId));
    }

}
