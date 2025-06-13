package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Objects;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.repository.ConcertRepository;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation.ReservationTokenIssueRequest;
import kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation.ReservationConfirmRequest;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class ReservationScenarioTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BalanceRepository balanceRepository;
    @Autowired private ConcertRepository concertRepository;
    @Autowired private ConcertScheduleRepository concertScheduleRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private User testUser;
    private Seat testSeat;
    private final long INITIAL_BALANCE = 100_000L;
    private final long SEAT_PRICE = 55_000L;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().build());
        balanceRepository.save(Balance.builder().userId(testUser.getId()).balance(INITIAL_BALANCE).build());

        Concert concert = concertRepository.save(Concert.builder().title("테스트 콘서트").build());
        ConcertSchedule schedule = concertScheduleRepository.save(ConcertSchedule.builder().concertId(concert.getId()).startAt(LocalDateTime.now().plusDays(10)).build());
        testSeat = seatRepository.save(Seat.builder().concertScheduleId(schedule.getId()).seatNo(15).price(SEAT_PRICE).status(Seat.SeatStatus.AVAILABLE).build());
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    @DisplayName("유저 토큰 발급부터 결제까지 전체 시나리오 테스트")
    void full_reservation_and_payment_scenario_test_with_mysql_testcontainers() throws Exception {
        // 토큰 발급 API 호출
        MvcResult tokenResult = mockMvc.perform(post("/api/queue/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservationTokenIssueRequest(testUser.getId()))))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        String token = tokenResult.getResponse().getContentAsString();//토큰

        //좌석 임시 예약 API 호출
        MvcResult reserveResult = mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .header("X-Queue-Token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReserveRequest(testUser.getId(), testSeat.getId(), testSeat.getConcertScheduleId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"))
                .andDo(print())
                .andReturn();

        Long reservationId = objectMapper.readTree(reserveResult.getResponse().getContentAsString()).get("id").asLong();

        //실제 예약 확정(결제) API 호출
        mockMvc.perform(post("/api/reservation/reserve-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservationConfirmRequest(testUser.getId(), reservationId))))
                .andExpect(status().isOk())
                .andDo(print());

        // 최종 상태 검증
        Balance finalBalance = balanceRepository.findByUserId(testUser.getId()).orElseThrow();
        assertThat(finalBalance.getBalance()).isEqualTo(INITIAL_BALANCE - SEAT_PRICE);

        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
