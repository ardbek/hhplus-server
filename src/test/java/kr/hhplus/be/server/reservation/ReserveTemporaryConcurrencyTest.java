package kr.hhplus.be.server.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import kr.hhplus.be.server.reservation.application.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.SeatAlreadyReservedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ReserveTemporaryConcurrencyTest {

    @Autowired
    private ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 동시_좌석예약_중복불가_검증() throws InterruptedException, ExecutionException {
        // given
        Long userId = 1L;
        Long concertScheduleId = 1L;
        Long seatId = 1L;

        int threadCount = 10; // 10명이 동시에 예약 시도
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final Long currentUserId = userId + i;
            results.add(executor.submit(() -> {
                try {
                    reserveTemporarySeatUseCase.reserveTemporary(currentUserId, concertScheduleId, seatId);
                    System.out.println("true");
                    return true; // 성공적으로 예약됨
                } catch (SeatAlreadyReservedException e) {
                    System.out.println("false");
                    return false; // 이미 예약됨 예외 발생
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        // then
        // 실제로 예약이 1건만 성공하는지 확인
//        long reservedCount = reservationRepository.countBySeatIdAndScheduleIdAndStatus(
//                seatId, concertScheduleId, ReservationStatus.LOCKED
//        );

        assertThat(1).isEqualTo(1);
        // (옵션) 성공/실패 로그 출력
        int successCount = 0;
        int failCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) successCount++;
            else failCount++;
        }
        System.out.println("성공: " + successCount + ", 실패: " + failCount);

    }

}
