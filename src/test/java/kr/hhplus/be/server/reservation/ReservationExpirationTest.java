package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation.ReservationRequest;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "schedules.cron.reservation.expiration=*/2 * * * * *") // ⭐️ 스케줄러를 2초마다 실행하도록 설정
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class ReservationExpirationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private ConcertScheduleRepository concertScheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private User userA;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder().build());
        Concert concert = concertRepository.save(Concert.builder().title("테스트 콘서트").build());
        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder().concertId(concert.getId()).startAt(LocalDateTime.now().plusDays(10)).build());
        testSeat = seatRepository.save(Seat.builder().concertScheduleId(schedule.getId()).seatNo(15).price(55000L).status(Seat.SeatStatus.AVAILABLE).build());
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    @DisplayName("임시 예약이 만료된 후 다른 사용자가 해당 좌석을 예약할 수 있다.")
    void after_reservation_expires_another_user_can_reserve_seat() throws Exception {
        // --- 1단계: UserA가 좌석을 임시 예약 ---
        ReservationRequest userARequest = new ReservationRequest(userA.getId(), testSeat.getId(), testSeat.getConcertScheduleId());

        MvcResult reserveResult = mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"))
                .andReturn();

        Long userAReservationId = objectMapper.readTree(reserveResult.getResponse().getContentAsString()).get("id").asLong();

        // --- 2단계: 예약이 만료될 때까지 대기 (Awaitility 사용) ---
        // 예약 만료 로직이 5분 뒤에 실행된다고 가정하고, 스케줄러가 이를 정리할 시간을 줍니다.
        // 테스트에서는 application.yml의 만료 시간을 1초 등으로 짧게 설정하고, 스케줄러 주기도 짧게 설정하는 것이 좋습니다.
        await().atMost(Duration.ofSeconds(20)) // 최대 10초까지 기다림
                .untilAsserted(() -> {
                    // 예약 상태가 RELEASED로 변경되었는지 확인
                    Reservation reservation = reservationRepository.findById(userAReservationId).orElseThrow();
                    assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);

                    // 좌석 상태가 AVAILABLE로 원복되었는지 확인
                    Seat seat = seatRepository.findById(testSeat.getId()).orElseThrow();
                    assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
                });

        // --- 3단계: UserB가 동일한 좌석을 예약 시도 ---
        User userB = userRepository.save(User.builder().build());
        ReservationRequest userBRequest = new ReservationRequest(userB.getId(), testSeat.getId(), testSeat.getConcertScheduleId());

        mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isOk()) // UserB가 성공적으로 예약
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }
}
