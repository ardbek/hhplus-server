package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.hhplus.be.server.reservation.application.reservation.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReserveTemporaryConcurrencyTest {

    @Autowired private ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SeatJpaRepository seatJpaRepository;
    @Autowired private ConcertScheduleJpaRepository concertScheduleJpaRepository;
    @Autowired private ConcertJpaRepository concertJpaRepository;

    private Long testConcertId;
    private Long testScheduleId;
    private Long testSeatId;

    @BeforeEach
    void setUp() {
        ConcertEntity concert = concertJpaRepository.save(ConcertEntity.builder()
                .title("테스트 콘서트")
                .build());
        testConcertId = concert.getId();

        ConcertScheduleEntity schedule = concertScheduleJpaRepository.save(ConcertScheduleEntity.builder()
                .concert(concert)
                .startAt(LocalDateTime.now().plusDays(1))
                .build());
        testScheduleId = schedule.getId();

        SeatEntity seatEntity = seatJpaRepository.save(SeatEntity.builder()
                .seatNo(1)
                .concertScheduleEntity(schedule)
                .price(50_000L)
                .build());
        testSeatId = seatEntity.getId();
    }

    @AfterEach
    void tearDown() {
        seatJpaRepository.deleteAll();
        concertScheduleJpaRepository.deleteAll();
        concertJpaRepository.deleteAll();
    }

    @Test
    void 동시_좌석예약_중복불가_검증() throws InterruptedException, ExecutionException {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<Boolean>> results = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long currentUserId = (long) (i + 1);
            results.add(executor.submit(() -> {
                try {
                    reserveTemporarySeatUseCase.reserveTemporary(currentUserId, testScheduleId, testSeatId);
                    return true; // 성공
                } catch (SeatAlreadyReservedException e) {
                    return false; // 실패
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();
        executor.shutdown();

        // then
        long successCount = results.stream().filter(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        }).count();

        System.out.println("성공: " + successCount);
        assertThat(successCount).isEqualTo(1);
    }
}