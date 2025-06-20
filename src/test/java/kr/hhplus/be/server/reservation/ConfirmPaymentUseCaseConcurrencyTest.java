package kr.hhplus.be.server.reservation;

import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.application.reservation.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.exception.balance.InsufficientBalanceException;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfirmPaymentUseCaseConcurrencyTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("hhplus");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private ConfirmPaymentUseCase confirmPaymentUseCase;
    @Autowired private UserRepository userRepository;
    @Autowired private BalanceRepository balanceRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    private User testUser;
    private List<Reservation> reservations;
    private Long testConcertScheduleId;
    private static final long SEAT_PRICE = 15000L;
    private static final long INITIAL_BALANCE = 100000L;

    @BeforeEach
    void setUp() {
        //테스트 유저 생성
        testUser = userRepository.save(User.builder().build());

        //해당 유저의 잔액 생성
        balanceRepository.save(Balance.builder()
                .userId(testUser.getId())
                .balance(INITIAL_BALANCE)
                .build());

        //콘서트와 스케줄 정보 생성
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder().title("테스트 콘서트").build());
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(ConcertScheduleEntity.builder()
                .concert(concert)
                .startAt(LocalDateTime.now().plusDays(1))
                .build());
        testConcertScheduleId = schedule.getId(); // 생성된 스케줄 ID 저장

        //결제를 시도할 10개의 좌석과 임시 예약 생성
        reservations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Seat seat = seatRepository.save(Seat.builder()
                    .concertScheduleId(testConcertScheduleId)
                    .seatNo(i + 1)
                    .price(SEAT_PRICE)
                    .build());

            Reservation reservation = reservationRepository.save(Reservation.builder()
                    .userId(testUser.getId())
                    .concertScheduleId(testConcertScheduleId)
                    .seatId(seat.getId())
                    .status(ReservationStatus.LOCKED)
                    .build());
            reservations.add(reservation);
        }
    }

    @Test
    @DisplayName("한 사용자가 동시에 여러 건의 결제를 시도할 때 잔액이 정확히 차감되어야 한다.")
    void concurrent_payment_test() throws InterruptedException {
        // given
        int numberOfThreads = 10; // 10개의 결제를 동시에 시도
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    Long reservationId = reservations.get(index).getId();
                    confirmPaymentUseCase.confirmReservation(testUser.getId(), reservationId);
                    successCount.incrementAndGet();
                } catch (InsufficientBalanceException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        //예상 성공/실패 횟수 검증
        int expectedSuccessCount = (int) (INITIAL_BALANCE / SEAT_PRICE);
        int expectedFailCount = numberOfThreads - expectedSuccessCount;

        assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        assertThat(failCount.get()).isEqualTo(expectedFailCount);

        // 최종 잔액 검증
        Balance finalBalance = balanceRepository.findByUserId(testUser.getId()).get();
        long expectedFinalBalance = INITIAL_BALANCE - (expectedSuccessCount * SEAT_PRICE);

        assertThat(finalBalance.getBalance()).isEqualTo(expectedFinalBalance);

        // 최종 예약 상태 검증
        List<Long> confirmedSeatIds = reservationRepository.findByReservedSeatIds(
                testConcertScheduleId, List.of(ReservationStatus.CONFIRMED));
        assertThat(confirmedSeatIds.size()).isEqualTo(expectedSuccessCount);
    }
}
