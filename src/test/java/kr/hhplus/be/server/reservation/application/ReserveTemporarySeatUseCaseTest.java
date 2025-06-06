package kr.hhplus.be.server.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.SeatAlreadyReservedException;
import kr.hhplus.be.server.reservation.exception.SeatNotFoundException;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ReserveTemporarySeatUseCaseTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    SeatRepository seatRepository;

    ReserveTemporarySeatUseCase reserveTemporarySeatUseCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        reserveTemporarySeatUseCase = new ReserveTemporarySeatUseCase(reservationRepository,
                seatRepository);
    }

    @Test
    @DisplayName("좌석 임시 예약에 성공한다.")
    void reserveTemporarySeat_success() {
        // given
        Long userId = 1L;
        Long concertScheduleId = 100L;
        Long seatId = 1L;

        given(reservationRepository.existsLocked(seatId, concertScheduleId)).willReturn(false);

        Reservation expected = Reservation.builder()
                .userId(userId)
                .concertScheduleId(concertScheduleId)
                .seatId(seatId)
                .status(ReservationStatus.LOCKED)
                .build();

        given(reservationRepository.save(any())).willReturn(expected);

        // when
        Reservation reservation = reserveTemporarySeatUseCase.reserveTemporary(userId, concertScheduleId, seatId);

        // then
        assertThat(reservation.getUserId()).isEqualTo(expected.getUserId());
        assertThat(reservation.getConcertScheduleId()).isEqualTo(concertScheduleId);
        assertThat(reservation.getSeatId()).isEqualTo(seatId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.LOCKED);
    }

    @Test
    @DisplayName("이미 임시 예약된 좌석이면 예외가 발생한다.")
    void reserveSeat_alreadyReserved() {
        // given
        Long userId = 1L;
        Long concertScheduleId = 100L;
        Long seatId = 10L;

        given(reservationRepository.existsLocked(seatId, concertScheduleId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reserveTemporarySeatUseCase.reserveTemporary(userId, concertScheduleId, seatId))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    @Test
    @DisplayName("좌석이 존재하지 않으면 예외가 발생한다.")
    void reserveSeat_seatNotFound() {
        // given
        Long seatId = 10L;
        given(seatRepository.findByIdForUpdate(seatId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                reserveTemporarySeatUseCase.reserveTemporary(1L, 100L, seatId))
                .isInstanceOf(SeatNotFoundException.class);
    }
}
