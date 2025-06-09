package kr.hhplus.be.server.reservation.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.exception.NotTemporaryReservationException;
import kr.hhplus.be.server.reservation.exception.NotYourReservationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationTest {

    @Test
    @DisplayName("예약 상태가 LOCKED 가 아니면 예외가 발생한다.")
    void confirm_fail_if_not_locked() {
        // given
        Reservation reservation = Reservation.builder().userId(1L)
                .status(ReservationStatus.CONFIRMED)
                .build();

        //when & then
        assertThatThrownBy(() -> reservation.confirm(1L))
                .isInstanceOf(NotTemporaryReservationException.class);
    }

    @Test
    @DisplayName("임시예약 소유자가 아닐 때 예약을 시도하면 예외가 발생한다.")
    void confirm_fail_if_not_owner() {
        // given
        Reservation reservation = Reservation.builder()
                .userId(1L)
                .status(ReservationStatus.LOCKED)
                .build();

        // when & then
        assertThatThrownBy(() -> reservation.confirm(2L))
                .isInstanceOf(NotYourReservationException.class);
    }
}
