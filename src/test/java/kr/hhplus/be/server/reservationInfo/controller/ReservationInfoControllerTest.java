package kr.hhplus.be.server.reservationInfo.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.interfaces.web.controller.ReservationController;
import kr.hhplus.be.server.reservationInfo.service.ReservationInfoQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReservationController.class)
public class ReservationInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationInfoQueryService reservationInfoQueryService;

    @Test
    @DisplayName("예약 가능한 날짜를 정상적으로 조회한다.")
    void getAvailableDates_success() throws Exception {
        // given
        Long concertId = 1L;
        List<LocalDate> dates = List.of(LocalDate.of(2025, 06, 10), LocalDate.of(2025, 06, 11),
                LocalDate.of(2025, 06, 12));

        given(reservationInfoQueryService.getAvailableDates(concertId)).willReturn(dates);

        // when & then
        mockMvc.perform(get("/api/reservation/{concertId}/dates", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value(1))
                .andExpect(jsonPath("$.availableDates").isArray());

    }

    @Test
    @DisplayName("예약 가능한 좌석을 정상적으로 조회한다.")
    void getAvailableSeats_success() throws Exception {
        // given
        Long scheduleId = 1L;

        List<SeatEntity> seatEntities = List.of(
                SeatEntity.builder()
                        .id(1L)
                        .seatNo(1)
                        .price(10_000L)
                        .concertScheduleEntity(null) // 테스트 목적이므로 null
                        .build()
        );

        given(reservationInfoQueryService.getAvailableSeats(scheduleId)).willReturn(seatEntities);

        // when & then
        mockMvc.perform(get("/api/reservation/schedules/{scheduleId}/seats", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(1))
                .andExpect(jsonPath("$.availableSeats[0].id").value(1))
                .andExpect(jsonPath("$.availableSeats[0].seatNo").value(1))
                .andExpect(jsonPath("$.availableSeats[0].price").value(10_000));
    }

}
