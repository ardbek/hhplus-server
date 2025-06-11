package kr.hhplus.be.server.reservation;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import kr.hhplus.be.server.reservation.application.reservation.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.seat.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
public class ReservationTemporarySeatEntityUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatJpaRepository seatJpaRepository;

    @InjectMocks
    private ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;

    @BeforeEach
    void setUp() {
        // Seat mock 설정
        given(seatJpaRepository.findByIdForUpdate(anyLong()))
            .willReturn(Optional.of(SeatEntity.builder().id(1L).build()));
    }

    @Test
    @DisplayName("동시 요청 시 오직 하나만 좌석 예약에 성공해야 한다")
    public void reserveSeat_concurrentAccess() throws InterruptedException {
        // given
        Long concertScheduleId = 100L;
        Long seatId = 1L;

        given(reservationRepository.existsLocked(seatId, concertScheduleId))
            .willAnswer(new Answer<Boolean>() {
                private final AtomicBoolean first = new AtomicBoolean(true);
                @Override
                public Boolean answer(InvocationOnMock invocation) {
                    return !first.getAndSet(false); // 첫 번째만 false
                }
            });

        given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Reservation> successes = Collections.synchronizedList(new ArrayList<>());
        List<Exception> failures = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    Reservation reservation = reserveTemporarySeatUseCase.reserveTemporary(userId, concertScheduleId, seatId);
                    successes.add(reservation);
                } catch (Exception e) {
                    failures.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successes).hasSize(1);  // 1명만 성공
        assertThat(failures).hasSize(threadCount - 1);  // 나머지는 실패
        assertThat(failures).allMatch(e -> e instanceof SeatAlreadyReservedException);
    }
}