package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation.ReserveRequest;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "schedules.cron.reservation.expiration=*/1 * * * * *", // 1초마다 스케줄러 실행
    "reservation.temporary.expiration-seconds=2" // 임시 예약 만료 시간을 2초로 설정
})
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationExpirationIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleRepository concertScheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;

    private User userA;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        userA = userRepository.save(User.builder().build());
        
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
                .title("테스트 콘서트")
                .build());

        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder()
                .concertId(concert.getId())
                .startAt(LocalDateTime.now().plusDays(1))
                .build());

        testSeat = seatRepository.save(Seat.builder()
                .concertScheduleId(schedule.getId())
                .seatNo(1)
                .price(50000L)
                .status(Seat.SeatStatus.AVAILABLE)
                .build());
    }

    @Test
    @DisplayName("임시 예약이 만료된 후 다른 사용자가 해당 좌석을 예약할 수 있다.")
    void after_reservation_expires_another_user_can_reserve_seat() throws Exception {
        // 첫 번째 사용자의 예약
        ReserveRequest userARequest = new ReserveRequest(userA.getId(), testSeat.getConcertScheduleId(), testSeat.getId());

        MvcResult reserveResult = mockMvc.perform(post("/api/reservation/reserve-temporary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userARequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("LOCKED"))
            .andReturn();

        long reservationId = objectMapper.readTree(reserveResult.getResponse().getContentAsString()).get("id").asLong();

        // 예약이 만료될 때까지 대기 (최대 10초)
        await().atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(100)) // 0.1초마다 상태 확인
            .untilAsserted(() -> {
                Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
                Seat seat = seatRepository.findById(testSeat.getId()).orElseThrow();

                // 예약 상태가 RELEASED로 변경되었는지 확인
                assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);
                // 좌석 상태가 AVAILABLE로 변경되었는지 확인
                assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
            });

        // 두 번째 사용자의 예약 시도
        User userB = userRepository.save(User.builder().build());
        ReserveRequest userBRequest = new ReserveRequest(userB.getId(), testSeat.getConcertScheduleId(), testSeat.getId());

        mockMvc.perform(post("/api/reservation/reserve-temporary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userBRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("LOCKED"));
    }
}