package kr.hhplus.be.server.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Set;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.reservation.application.reservation.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.reservation.port.out.DataPlatformApiClient;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;
import kr.hhplus.be.server.reservation.domain.model.Seat.SeatStatus;
import kr.hhplus.be.server.reservation.infrastructure.persistence.balance.BalanceEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.balance.BalanceJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.payment.PaymentJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class ConfirmPaymentIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>("redis:7.2-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired private UserRepository userJpaRepository;
    @Autowired private BalanceJpaRepository balanceJpaRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired private SeatJpaRepository seatJpaRepository;
    @Autowired private ReservationJpaRepository reservationJpaRepository;
    @Autowired private PaymentJpaRepository paymentJpaRepository;
    @Autowired private BalanceHistoryRepository balanceHistoryRepository;

    @MockitoSpyBean
    private DataPlatformApiClient dataPlatformApiClient;

    private final String RANKING_KEY = "concert:schedule:ranking";

    private User user1, user2;
    private ConcertScheduleEntity schedule;
    private SeatEntity seat2;
    private ReservationEntity lastSeatReservation;
    private ConcertEntity concert;

    @BeforeEach
    void setUp() {
        // 테스트 전 Redis 데이터 초기화
        redisTemplate.delete(RANKING_KEY);
        // 테스트 데이터 준비
        setupDatabase();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setupDatabase() {

        paymentJpaRepository.deleteAllInBatch();
        balanceHistoryRepository.deleteAllInBatch();

        reservationJpaRepository.deleteAllInBatch();
        seatJpaRepository.deleteAllInBatch();
        concertScheduleJpaRepository.deleteAllInBatch();
        concertJpaRepository.deleteAllInBatch();
        balanceJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();

        user1 = userJpaRepository.save(User.builder().build());
        user2 = userJpaRepository.save(User.builder().build());
        balanceJpaRepository.save(BalanceEntity.builder().user(user1).balance(100_000L).build());

        concert = concertJpaRepository.save(ConcertEntity.builder().title("테스트 콘서트").build());
        schedule = concertScheduleJpaRepository.save(
                ConcertScheduleEntity.builder().concert(concert)
                        .startAt(LocalDateTime.now().minusMinutes(1))
                        .ticketOpenTime(LocalDateTime.now().minusHours(5))
                        .build()
        );

        SeatEntity seat1 = seatJpaRepository.save(
                SeatEntity.builder().concertScheduleEntity(schedule).seatNo(1).price(50_000L)
                        .status(SeatStatus.PAID).build());
        seat2 = seatJpaRepository.save(
                SeatEntity.builder().concertScheduleEntity(schedule).seatNo(2).price(50_000L)
                        .status(SeatStatus.RESERVED).build());

        reservationJpaRepository.save(
                new ReservationEntity(null, user2, schedule, seat1, ReservationStatus.CONFIRMED, LocalDateTime.now(), LocalDateTime.now())
        );

        lastSeatReservation = reservationJpaRepository.save(
                new ReservationEntity(null, user1, schedule, seat2, ReservationStatus.LOCKED, LocalDateTime.now(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("결제 확정 시, 데이터 전송 이벤트와 매진 이벤트가 모두 정상 처리된다.")
    void confirm_payment_and_handle_all_events() {
        // when
        confirmPaymentUseCase.confirmReservation(user1.getId(), lastSeatReservation.getId(), seat2.getId());

        // then
        // 데이터 플랫폼 전송 이벤트 검증
        verify(dataPlatformApiClient, timeout(2000).times(1)).sendReservationData(any(ReservationConfirmedEvent.class));

        Set<TypedTuple<String>> ranking = redisTemplate.opsForZSet().rangeWithScores(RANKING_KEY, 0, -1);
        assertNotNull(ranking);
        assertEquals(1, ranking.size());

        TypedTuple<String> recordedRank = ranking.iterator().next();
        String expectedMember = "concert:" + concert.getId() + ":schedule:" + schedule.getId();
        assertEquals(expectedMember, recordedRank.getValue());
        assertTrue(recordedRank.getScore() > 0);
    }

}
