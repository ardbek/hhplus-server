package kr.hhplus.be.server.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableSeatsUseCase;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationInfoJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAvailableSeatsUseCaseTest {

    @InjectMocks
    private GetAvailableSeatsUseCase getAvailableSeatsUseCase;

    @Mock
    private ReservationInfoJpaRepository reservationInfoJpaRepository;

    @Mock
    private SeatJpaRepository seatJpaRepository;

    private SeatEntity createTestSeat(Long seatId, int seatNo, Long price) {
        return SeatEntity.builder()
            .id(seatId)
            .seatNo(seatNo)
            .price(price)
            .concertScheduleEntity(null)
            .build();
    }

    @Test
    @DisplayName("예약 가능 좌석을 정상적으로 조회한다.")
    void getAvailableSeats_success() {
        // given
        Long scheduleId = 1L;

        SeatEntity seatEntity1 = createTestSeat(1L, 1, 10_000L);
        SeatEntity seatEntity2 = createTestSeat(2L, 2, 15_000L);
        SeatEntity seatEntity3 = createTestSeat(3L, 3, 20_000L);

        List<SeatEntity> allSeatEntities = List.of(seatEntity1, seatEntity2, seatEntity3);
        List<Long> reservedSeatIds = List.of(2L); // 2번 좌석은 예약됨

        List<ReservationStatus> reservedStatuses = ReservationStatus.reservedStatuses();

        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, reservedStatuses)).willReturn(reservedSeatIds);

        // when
        List<SeatEntity> availableSeatEntities = getAvailableSeatsUseCase.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeatEntities)
            .extracting(SeatEntity::getId)
            .containsExactlyInAnyOrder(1L, 3L);
    }

    @DisplayName("예약된 좌석이 없는 경우, 모든 좌석이 예약 가능해야 한다.")
    @Test
    void getAvailableSeats_noReservations() {
        // given
        Long scheduleId = 1L;
        List<SeatEntity> allSeatEntities = List.of(
            createTestSeat(1L, 1, 10_000L),
            createTestSeat(2L, 2, 15_000L)
        );
        List<ReservationStatus> reservedStatuses = ReservationStatus.reservedStatuses();

        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, reservedStatuses)).willReturn(List.of());

        // when
        List<SeatEntity> availableSeatEntities = getAvailableSeatsUseCase.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeatEntities).containsExactlyInAnyOrderElementsOf(allSeatEntities);
    }

    @DisplayName("모든 좌석이 예약된 경우, 예약 가능 좌석은 없어야 한다.")
    @Test
    void getAvailableSeats_allReserved() {
        // given
        Long scheduleId = 1L;
        List<SeatEntity> allSeatEntities = List.of(
            createTestSeat(1L, 1, 10_000L),
            createTestSeat(2L, 2, 15_000L)
        );
        List<Long> reservedIds = allSeatEntities.stream().map(SeatEntity::getId).toList();
        List<ReservationStatus> reservedStatuses = ReservationStatus.reservedStatuses();


        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, reservedStatuses)).willReturn(reservedIds);

        // when
        List<SeatEntity> availableSeatEntities = getAvailableSeatsUseCase.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeatEntities).isEmpty();
    }
}