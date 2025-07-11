package kr.hhplus.be.server.reservation.application.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Optional;

import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.event.ReservationConfirmedEvent;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.exception.reservation.NotTemporaryReservationException;
import kr.hhplus.be.server.reservation.exception.reservation.NotYourReservationException;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

public class ConfirmPaymentUseCaseTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BalanceRepository balanceRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private ReservationTokenRepository reservationTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

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
        given(reservation.getUserId()).willReturn(userId);
        given(reservation.isLocked()).willReturn(true);
        given(reservation.getSeatId()).willReturn(seatId);
        given(reservation.getId()).willReturn(reservationId);

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        Seat seat = Seat.builder().id(seatId).price(price).build();
        User user = User.builder().id(userId).build();
        Balance balance = Balance.builder().id(100L).userId(user.getId()).balance(10_000L).build();

        given(seatRepository.findById(seatId)).willReturn(Optional.of(seat));
        given(balanceRepository.findByUserIdForUpdate(userId)).willReturn(Optional.of(balance));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        confirmPaymentUseCase.confirmReservation(userId, reservationId, seatId);

        // then
        assertThat(reservationTokenRepository.isActiveUser(userId)).isEqualTo(false);
        verify(balanceRepository).save(any(Balance.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
        verify(reservationRepository).save(any(Reservation.class));

        // 발생한 이벤트 캡처
        ArgumentCaptor<ReservationConfirmedEvent> eventCaptor = ArgumentCaptor.forClass(ReservationConfirmedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        // 이벤트 검증
        ReservationConfirmedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.reservationId()).isEqualTo(reservationId);
        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.seatId()).isEqualTo(seatId);
        assertThat(capturedEvent.price()).isEqualTo(price);
    }

    @Test
    @DisplayName("예약 상태가 LOCKED가 아니면 예외가 발생한다.")
    void confirm_fail_if_not_locked() {
        // given
        Long ownerUserId = 1L;
        // 상태가 이미 확정(CONFIRMED)된 예약 객체
        Reservation reservation = Reservation.builder()
                .userId(ownerUserId)
                .status(ReservationStatus.CONFIRMED)
                .build();

        // when & then
        assertThatThrownBy(() -> reservation.confirm(ownerUserId))
                .isInstanceOf(NotTemporaryReservationException.class);
    }

    @Test
    @DisplayName("예약 소유자가 아니면 예외가 발생한다.")
    void confirm_fail_if_not_owner() {
        // given
        Long ownerUserId = 1L;
        Long otherUserId = 2L; // 다른 사용자
        Reservation reservation = Reservation.builder()
                .userId(ownerUserId)
                .status(ReservationStatus.LOCKED)
                .build();

        // when & then
        assertThatThrownBy(() -> reservation.confirm(otherUserId))
                .isInstanceOf(NotYourReservationException.class);
    }
}