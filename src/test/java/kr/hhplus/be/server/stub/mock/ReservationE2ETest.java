package kr.hhplus.be.server.stub.mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
class ReservationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 전체_예매_흐름_E2E() throws Exception {
        // 1. 유저 토큰 발급
        String tokenResponse = mockMvc.perform(post("/mock/queue-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "userId": "1"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdingToken").value("mock-token-12345678")) // 토큰 확인
                .andReturn().getResponse().getContentAsString();

        // 2. 예약 가능 날짜 조회
        mockMvc.perform(get("/mock/reservation/1/dates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value("1"))
                .andExpect(jsonPath("$.availableDates").isArray());

        // 3. 예약 가능 좌석 조회
        mockMvc.perform(get("/mock/reservation/1/dates/2025-07-01/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value("1"))
                .andExpect(jsonPath("$.date").value("2025-07-01"))
                .andExpect(jsonPath("$.availableSeats").isArray());

        // 4. 좌석 예약
        mockMvc.perform(post("/mock/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "userId": "1",
                      "concertId": "1",
                      "reservationDate": "2025-07-01",
                      "seatId": 10
                    }
                """))
                .andExpect(status().isOk());

        // 5. 결제
        mockMvc.perform(post("/mock/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "userId": "1",
                      "token": "mock-token-12345678",
                      "seatId": 1
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1")) // 예약 사용자 확인
                .andExpect(jsonPath("$.status").value("SUCCESS")) // 예약 상태 확인
                .andExpect(jsonPath("$.tokenExpired").value(true)); // 토큰 만료 여부 확인
    }
}

