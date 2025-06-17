package kr.hhplus.be.server.reservation.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.exception.seat.SeatNotFoundException;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReserveTemporarySeatUseCaseTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("hhplus");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private ConcertJpaRepository concertJpaRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Seat testSeat;
    private ConcertScheduleEntity testConcertSchedule;
    private ConcertEntity testConcert;
    private static final Long TEST_USER_ID = 1L;
    private Long testConcertScheduleId;

    @BeforeEach
    void setUp() {
        testUser = User.builder().build();
        testUser = userRepository.save(this.testUser);

        // 테스트용 콘서트 생성
        testConcert = ConcertEntity.builder()
                .title("테스트 콘서트")
                .build();
        testConcert = concertJpaRepository.save(testConcert);

        // 테스트용 콘서트 일정 생성
        testConcertSchedule = ConcertScheduleEntity.builder()
                .concert(testConcert)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();
        testConcertSchedule = concertScheduleJpaRepository.save(testConcertSchedule);
        testConcertScheduleId = testConcertSchedule.getId();

        // 테스트용 좌석 생성
        testSeat = Seat.builder()
                .concertScheduleId(testConcertScheduleId)
                .seatNo(1)
                .price(10000L)
                .build();
        testSeat = seatRepository.save(testSeat);
    }

    @Test
    @DisplayName("단일 예약이 정상적으로 처리되어야 한다")
    @Transactional
    void singleReservation() {
        // when
        Reservation reservation = reserveTemporarySeatUseCase.reserveTemporary(testUser.getId(), testConcertSchedule.getId(), testSeat.getId());

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getSeatId()).isEqualTo(testSeat.getId());
        assertThat(reservation.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(reservation.getConcertScheduleId()).isEqualTo(testConcertScheduleId);
    }

    @Test
    @DisplayName("동시에 여러 예약 요청이 들어올 때 한 번에 하나의 예약만 성공해야 한다")
    void concurrentReservation() throws InterruptedException {
        // given
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            final Long userId = (long) i + 1;
            executorService.submit(() -> {
                try {
                    reserveTemporarySeatUseCase.reserveTemporary(userId, testConcertScheduleId, testSeat.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(exceptions.size()).isEqualTo(numberOfThreads - 1);
        assertThat(exceptions).allMatch(e -> e instanceof SeatAlreadyReservedException);
    }

    @Test
    @DisplayName("존재하지 않는 좌석에 대한 예약 시도 시 예외가 발생해야 한다")
    @Transactional
    void reservationWithNonExistentSeat() {
        // given
        Long nonExistentSeatId = 999L;

        // when & then
        assertThatThrownBy(() ->
            reserveTemporarySeatUseCase.reserveTemporary(TEST_USER_ID, testConcertScheduleId, nonExistentSeatId)
        ).isInstanceOf(SeatNotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 좌석에 대한 예약 시도 시 예외가 발생해야 한다")
    @Transactional
    void reservationWithAlreadyReservedSeat() {
        // given
        // 첫 번째 사용자가 좌석을 예약합니다. (이 사용자는 @BeforeEach에서 생성되었다고 가정)
        reserveTemporarySeatUseCase.reserveTemporary(testUser.getId(), testConcertScheduleId, testSeat.getId());

        // [수정] 두 번째 예약을 시도할 다른 사용자를 생성하고 DB에 저장합니다.
        User anotherUser = userRepository.save(User.builder().build());

        // when & then
        // [수정] 미리 생성한 다른 사용자의 ID로 예약을 시도합니다.
        assertThatThrownBy(() ->
                reserveTemporarySeatUseCase.reserveTemporary(anotherUser.getId(), testConcertScheduleId, testSeat.getId())
        ).isInstanceOf(SeatAlreadyReservedException.class);
    }
}