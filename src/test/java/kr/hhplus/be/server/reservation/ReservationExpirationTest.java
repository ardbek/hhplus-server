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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "schedules.cron.reservation.expiration=*/2 * * * * *") // ⭐️ 스케줄러를 2초마다 실행하도록 설정
@AutoConfigureMockMvc
@Testcontainers
class ReservationExpirationTest {

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
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private User userA;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder().build());
        
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
                .title("테스트 콘서트")
                .build());

        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder()
                .concertId(concert.getId())
                .startAt(LocalDateTime.now().plusDays(10))
                .build());

        testSeat = seatRepository.save(Seat.builder()
                .concertScheduleId(schedule.getId())
                .seatNo(15)
                .price(55000L)
                .status(Seat.SeatStatus.AVAILABLE)
                .build());
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    @DisplayName("임시 예약이 만료된 후 다른 사용자가 해당 좌석을 예약할 수 있다.")
    void after_reservation_expires_another_user_can_reserve_seat() throws Exception {
        // --- 1단계: UserA가 좌석을 임시 예약 ---
        ReserveRequest userARequest = new ReserveRequest(userA.getId(), testSeat.getId(), testSeat.getConcertScheduleId());

        MvcResult reserveResult = mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userARequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"))
                .andReturn();

        Long userAReservationId = objectMapper.readTree(reserveResult.getResponse().getContentAsString()).get("id").asLong();

        // --- 2단계: 예약이 만료될 때까지 대기 (Awaitility 사용) ---
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    Reservation reservation = reservationRepository.findById(userAReservationId).orElseThrow();
                    assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.LOCKED);

                    Seat seat = seatRepository.findById(testSeat.getId()).orElseThrow();
                    assertThat(seat.getStatus()).isEqualTo(Seat.SeatStatus.AVAILABLE);
                });

        // --- 3단계: UserB가 동일한 좌석을 예약 시도 ---
        User userB = userRepository.save(User.builder().build());
        ReserveRequest userBRequest = new ReserveRequest(userB.getId(), testSeat.getId(), testSeat.getConcertScheduleId());

        mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBRequest)))
                .andExpect(status().isOk()) // UserB가 성공적으로 예약
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }
}
