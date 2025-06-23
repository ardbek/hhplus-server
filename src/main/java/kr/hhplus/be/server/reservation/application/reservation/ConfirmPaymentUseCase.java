package kr.hhplus.be.server.reservation.application.reservation;

import java.time.LocalDateTime;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistoryType;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.exception.balance.BalanceNotFoundException;
import kr.hhplus.be.server.reservation.exception.reservation.ReservationNotFoundException;
import kr.hhplus.be.server.reservation.exception.seat.SeatNotFoundException;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationTokenRepository reservationTokenRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceRepository balanceRepository;
    private final SeatJpaRepository seatJpaRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final UserRepository userRepository;

    public ConfirmPaymentUseCase(ReservationRepository reservationRepository,
            ReservationTokenRepository reservationTokenRepository,
            PaymentRepository paymentRepository, BalanceRepository balanceRepository,
            SeatJpaRepository seatJpaRepository, BalanceHistoryRepository balanceHistoryRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTokenRepository = reservationTokenRepository;
        this.paymentRepository = paymentRepository;
        this.balanceRepository = balanceRepository;
        this.seatJpaRepository = seatJpaRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void confirmReservation(Long userId, Long reservationId) {
        // 1. 도메인 객체 조회
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        SeatEntity seatEntity = seatJpaRepository.findById(reservation.getSeatId()).orElseThrow(SeatNotFoundException::new);
        Balance balanceEntity = balanceRepository.findByUserIdForUpdate(userId).orElseThrow(BalanceNotFoundException::new);

        // 2. 예약 확정, 결제 처리
        reservation.confirm(user.getId());
        balanceEntity.pay(seatEntity.getPrice());

        // 3. 예약, 잔고 저장
        reservationRepository.save(reservation);
        balanceRepository.save(balanceEntity);

        // 4. 결제 내역 생성, 저장
        createPayment(reservation, seatEntity.getPrice());
        createBalanceHistory(user, seatEntity.getPrice(), balanceEntity.getBalance());

        expireQueueToken(userId);
    }

    private void createPayment(Reservation reservation, Long amount) {
        Payment payment = Payment.builder()
                .userId(reservation.getUserId())
                .reservationId(reservation.getId())
                .price(amount)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
    }

    private void createBalanceHistory(User user, long amount, long balanceAfter) {
        BalanceHistory history = BalanceHistory.builder()
                .user(user)
                .type(BalanceHistoryType.USE)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build();
        balanceHistoryRepository.save(history);
    }

    private void expireQueueToken(Long userId) {
        reservationTokenRepository.removeFromWaiting(userId);
    }

}
