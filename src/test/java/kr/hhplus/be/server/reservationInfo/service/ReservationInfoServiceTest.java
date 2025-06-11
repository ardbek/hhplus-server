package kr.hhplus.be.server.reservationInfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationInfoJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.reservationInfo.service.impl.ReservationInfoQueryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationInfoServiceTest {

    @InjectMocks
    private ReservationInfoQueryServiceImpl reservationInfoQueryServiceImpl;

    @Mock
    private ReservationInfoJpaRepository reservationInfoJpaRepository;

    @Mock
    private SeatJpaRepository seatJpaRepository;

    @Mock
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    private ConcertScheduleEntity createTestSchedule(Long scheduleId, Long concertId,
            LocalDateTime startAt) {
        return ConcertScheduleEntity.builder()
                .id(scheduleId)
                .concert(null)
                .startAt(startAt)
                .build();
    }

    private SeatEntity createTestSeat(Long seatId, int seatNo, Long price) {
        return SeatEntity.builder()
                .id(seatId)
                .seatNo(seatNo)
                .price(price)
                .concertScheduleEntity(null)
                .build();
    }

    @Test
    @DisplayName("예약 가능 날짜를 정상적으로 조회한다.")
    void getAvailableDates_success() {
        // given
        Long concertId = 1L;
        LocalDateTime concertDate1 = LocalDateTime.of(2025, 6, 10, 18, 0);
        LocalDateTime concertDate2 = LocalDateTime.of(2025, 6, 11, 18, 0);
        LocalDateTime concertDate3 = LocalDateTime.of(2025, 6, 12, 18, 0);
        LocalDateTime concertDate4 = LocalDateTime.of(2025, 6, 12, 19, 0);

        List<ConcertScheduleEntity> schedules = List.of(
                createTestSchedule(1L, concertId, concertDate1),
                createTestSchedule(2L, concertId, concertDate2),
                createTestSchedule(3L, concertId, concertDate3),
                createTestSchedule(4L, concertId, concertDate4)
        );

        given(concertScheduleJpaRepository.findByConcertId(concertId)).willReturn(schedules);

        // when
        List<LocalDate> availableDates = reservationInfoQueryServiceImpl.getAvailableDates(concertId);

        // then
        assertThat(availableDates).containsExactly(
                LocalDate.of(2025, 6, 10),
                LocalDate.of(2025, 6, 11),
                LocalDate.of(2025, 6, 12)
        );

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
        List<Long> reservedSeatIds = List.of(2L); // seat2 예약됨

        List<ReservationStatus> availableStatus = List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED);

        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId,availableStatus)).willReturn(reservedSeatIds);

        // when
        List<SeatEntity> availableSeatEntities = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

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

        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED)))
                .willReturn(List.of());

        // when
        List<SeatEntity> availableSeatEntities = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

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
        List<Long> reserved = allSeatEntities.stream().map(SeatEntity::getId).toList();

        given(seatJpaRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeatEntities);
        given(reservationInfoJpaRepository.findByReservedSeatIds(scheduleId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED)))
                .willReturn(reserved);

        // when
        List<SeatEntity> availableSeatEntities = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeatEntities).isEmpty();
    }
}
