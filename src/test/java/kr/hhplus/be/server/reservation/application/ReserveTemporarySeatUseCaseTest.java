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
import kr.hhplus.be.server.reservationInfo.domain.Seat;
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
    @DisplayName("좌석이 예약되지 않았을 때, 임시 예약 객체를 성공적으로 생성한다.")
    void reserveTemporary_success() {
        // given
        Long userId = 1L;
        Long concertScheduleId = 100L;
        Long seatId = 50L;
        boolean isAlreadyReserved = false; // "이미 예약되었는가?" -> 아니오

        // when
        Reservation reservation = Reservation.reserveTemporary(userId, concertScheduleId, seatId, isAlreadyReserved);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.LOCKED);
        assertThat(reservation.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("좌석이 이미 예약되었을 때, SeatAlreadyReservedException 예외를 발생시킨다.")
    void reserveTemporary_fail_if_already_reserved() {
        // given
        Long userId = 1L;
        Long concertScheduleId = 100L;
        Long seatId = 50L;
        boolean isAlreadyReserved = true; // "이미 예약되었는가?" -> 네

        // when & then
        // Reservation.reserveTemporary 메서드를 직접 호출하여 예외 발생을 검증
        assertThatThrownBy(() -> Reservation.reserveTemporary(userId, concertScheduleId, seatId, isAlreadyReserved))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }
}
