package kr.hhplus.be.server.reservation.application.reservation;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Seat.SeatStatus;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ExpireSchedulerConcurrencyTest {

    @Autowired
    private ExpireReservationUseCase expireReservationUseCase;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConcertJpaRepository concertJpaRepository;
    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired
    private SeatJpaRepository seatJpaRepository;
    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("여러 스케줄러가 동시에 실행되어도 만료된 예약은 중복 처리되지 않아야 한다.")
    void expireReservations_concurrency_test() throws InterruptedException {
        int totalExpiredCount = 10;
        User user = userRepository.save(User.builder().build());
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder().title("테스트 콘서트").build());
        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(ConcertScheduleEntity.builder().concert(concert).startAt(LocalDateTime.now().plusDays(1)).build());

        for (int i = 0; i < totalExpiredCount; i++) {
            SeatEntity seat = seatJpaRepository.save(SeatEntity.builder().concertScheduleEntity(schedule).seatNo(i+1).price(10_000L).status(SeatStatus.RESERVED).build());

            reservationJpaRepository.save(
                    ReservationEntity.builder()
                            .user(user)
                            .concertScheduleEntity(schedule)
                            .seatEntity(seat)
                            .status(ReservationStatus.LOCKED)
                            .createdAt(LocalDateTime.now().minusMinutes(10))
                            .build()
            );
        }

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    expireReservationUseCase.expireReservations();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Long canceledReservationCount = entityManager.createQuery(
                        "SELECT count(r) FROM ReservationEntity r WHERE r.status = :status", Long.class)
                .setParameter("status", ReservationStatus.RELEASED)
                .getSingleResult();

        Long availableSeatCount = entityManager.createQuery(
                        "SELECT count(s) FROM SeatEntity s WHERE s.status = :status", Long.class)
                .setParameter("status", SeatStatus.AVAILABLE)
                .getSingleResult();

        assertThat(canceledReservationCount).isEqualTo(totalExpiredCount);
        assertThat(availableSeatCount).isEqualTo(totalExpiredCount);
    }
}