package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleRepository concertScheduleRepository;
    @Autowired private SeatRepository seatRepository;

    private Seat testSeat;
    private List<User> users;

    @BeforeEach
    void setUp() {
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
                .title("동시성 테스트 콘서트")
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

        users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(userRepository.save(User.builder().build()));
        }
    }

    @Test
    @DisplayName("10명의 유저가 동시에 같은 좌석 예약을 요청하면 1명만 성공한다.")
    void when_multiple_users_request_same_seat_only_one_succeeds() throws InterruptedException, ExecutionException {
        // given
        int threadCount = users.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        // when
        for (User user : users) {
            Future<Integer> future = executorService.submit(() -> {
                try {
                    ReserveRequest request = new ReserveRequest(user.getId(), testSeat.getConcertScheduleId(), testSeat.getId());
                    ResultActions result = mockMvc.perform(post("/api/reservation/reserve-temporary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
                    return result.andReturn().getResponse().getStatus();
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        latch.await();
        executorService.shutdown();

        // then
        List<Integer> statusCodes = futures.stream()
            .map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return 500;
                }
            })
            .collect(Collectors.toList());

        long successCount = statusCodes.stream().filter(status -> status == 200).count();

        System.out.println("Response Statuses: " + statusCodes);
        System.out.println("Success Count: " + successCount);

        assertThat(successCount).isEqualTo(1);
    }
}