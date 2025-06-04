package kr.hhplus.be.server.reservationInfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.reservationInfo.domain.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservationInfo.repository.ReservationInfoRepository;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
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
    private ReservationInfoRepository reservationInfoRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    private ConcertSchedule createTestSchedule(Long scheduleId, Long concertId,
            LocalDateTime startAt) {
        return ConcertSchedule.builder()
                .id(scheduleId)
                .concert(null)
                .startAt(startAt)
                .build();
    }

    private Seat createTestSeat(Long seatId, int seatNo, Long price) {
        return Seat.builder()
                .id(seatId)
                .seatNo(seatNo)
                .price(price)
                .concertSchedule(null)
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

        List<ConcertSchedule> schedules = List.of(
                createTestSchedule(1L, concertId, concertDate1),
                createTestSchedule(2L, concertId, concertDate2),
                createTestSchedule(3L, concertId, concertDate3),
                createTestSchedule(4L, concertId, concertDate4)
        );

        given(concertScheduleRepository.findByConcertId(concertId)).willReturn(schedules);

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

        Seat seat1 = createTestSeat(1L, 1, 10_000L);
        Seat seat2 = createTestSeat(2L, 2, 15_000L);
        Seat seat3 = createTestSeat(3L, 3, 20_000L);

        List<Seat> allSeats = List.of(seat1, seat2, seat3);
        List<Long> reservedSeatIds = List.of(2L); // seat2 예약됨

        List<ReservationStatus> availableStatus = List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED);

        given(seatRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeats);
        given(reservationInfoRepository.findByReservedSeatIds(scheduleId,availableStatus)).willReturn(reservedSeatIds);

        // when
        List<Seat> availableSeats = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeats)
                .extracting(Seat::getId)
                .containsExactlyInAnyOrder(1L, 3L);

    }

    @DisplayName("예약된 좌석이 없는 경우, 모든 좌석이 예약 가능해야 한다.")
    @Test
    void getAvailableSeats_noReservations() {
        // given
        Long scheduleId = 1L;
        List<Seat> allSeats = List.of(
                createTestSeat(1L, 1, 10_000L),
                createTestSeat(2L, 2, 15_000L)
        );

        given(seatRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeats);
        given(reservationInfoRepository.findByReservedSeatIds(scheduleId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED)))
                .willReturn(List.of());

        // when
        List<Seat> availableSeats = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeats).containsExactlyInAnyOrderElementsOf(allSeats);
    }

    @DisplayName("모든 좌석이 예약된 경우, 예약 가능 좌석은 없어야 한다.")
    @Test
    void getAvailableSeats_allReserved() {
        // given
        Long scheduleId = 1L;
        List<Seat> allSeats = List.of(
                createTestSeat(1L, 1, 10_000L),
                createTestSeat(2L, 2, 15_000L)
        );
        List<Long> reserved = allSeats.stream().map(Seat::getId).toList();

        given(seatRepository.findByConcertScheduleId(scheduleId)).willReturn(allSeats);
        given(reservationInfoRepository.findByReservedSeatIds(scheduleId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.LOCKED)))
                .willReturn(reserved);

        // when
        List<Seat> availableSeats = reservationInfoQueryServiceImpl.getAvailableSeats(scheduleId);

        // then
        assertThat(availableSeats).isEmpty();
    }
}
