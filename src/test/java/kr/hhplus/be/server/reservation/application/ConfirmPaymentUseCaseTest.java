package kr.hhplus.be.server.reservation.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.*;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import kr.hhplus.be.server.wallet.domain.Balance;
import kr.hhplus.be.server.wallet.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfirmPaymentUseCaseTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BalanceRepository balanceRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private QueueTokenRepository queueTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("결제를 정상적으로 처리한다.")
    void confirmPayment_success() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;
        Long seatId = 20L;
        long price = 5000L;

        Reservation reservation = mock(Reservation.class);
        when(reservation.getUserId()).thenReturn(userId);
        when(reservation.isLocked()).thenReturn(true);
        when(reservation.getSeatId()).thenReturn(seatId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Seat seat = Seat.builder().id(seatId).price(price).build();
        User user = User.builder().id(userId).build();
        Balance balance = Balance.builder().id(100L).user(user).balance(10_000L).build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(balanceRepository.findByUserId(userId)).thenReturn(Optional.of(balance));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));


        // when
        confirmPaymentUseCase.confirmReservation(userId, reservationId);

        // then
        verify(balanceRepository).save(any(Balance.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
        verify(queueTokenRepository).expireTokenByUserId(eq(userId), eq(TokenStatus.EXPIRED), any(), eq(TokenStatus.ACTIVE));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("임시 예약이 아닌 경우 예외가 발생한다.")
    void confirmPayment_notTemporary() {
        Long userId = 1L;
        Long reservationId = 10L;
        Reservation reservation = mock(Reservation.class);
        when(reservation.getUserId()).thenReturn(userId);
        when(reservation.isLocked()).thenReturn(false);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> confirmPaymentUseCase.confirmReservation(userId, reservationId))
            .isInstanceOf(NotTemporaryReservationException.class);
    }

    @Test
    @DisplayName("타인의 예약에 접근하면 예외가 발생한다.")
    void confirmPayment_notYourReservation() {
        Long userId = 1L;
        Long reservationId = 10L;
        Reservation reservation = mock(Reservation.class);
        when(reservation.getUserId()).thenReturn(2L);
        when(reservation.isLocked()).thenReturn(true);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> confirmPaymentUseCase.confirmReservation(userId, reservationId))
            .isInstanceOf(NotYourReservationException.class);
    }
}