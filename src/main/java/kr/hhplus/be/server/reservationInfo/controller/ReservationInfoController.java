package kr.hhplus.be.server.reservationInfo.controller;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.dto.response.AvailableDatesResponse;
import kr.hhplus.be.server.reservationInfo.dto.response.AvailableSeatsResponse;
import kr.hhplus.be.server.reservationInfo.service.ReservationInfoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationInfoController {

    private final ReservationInfoQueryService reservationInfoQueryService;

    // 예약 가능 날짜 조회
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<AvailableDatesResponse> getAvailableDates(@PathVariable Long concertId) {
        List<LocalDate> availableDates = reservationInfoQueryService.getAvailableDates(concertId);
        return ResponseEntity.ok(new AvailableDatesResponse(concertId, availableDates));
    }


    // 예약 가능 좌석 조회
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(@PathVariable Long scheduleId) {
        List<Seat> availableSeats = reservationInfoQueryService.getAvailableSeats(scheduleId);
        return ResponseEntity.ok(new AvailableSeatsResponse(scheduleId, availableSeats));
    }

}
