package kr.hhplus.be.server.reservation.application;

import java.time.LocalDateTime;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistoryType;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.exception.BalanceNotFoundException;
import kr.hhplus.be.server.reservation.exception.InsufficientBalanceException;
import kr.hhplus.be.server.reservation.exception.InvalidSeatPriceException;
import kr.hhplus.be.server.reservation.exception.NotTemporaryReservationException;
import kr.hhplus.be.server.reservation.exception.NotYourReservationException;
import kr.hhplus.be.server.reservation.exception.ReservationNotFoundException;
import kr.hhplus.be.server.reservation.exception.SeatNotFoundException;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;
import kr.hhplus.be.server.wallet.domain.Balance;
import kr.hhplus.be.server.wallet.repository.BalanceRepository;
import org.springframework.transaction.annotation.Transactional;


public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceRepository balanceRepository;
    private final SeatRepository seatRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final UserRepository userRepository;

    public ConfirmPaymentUseCase(ReservationRepository reservationRepository,
            PaymentRepository paymentRepository, BalanceRepository balanceRepository,
            SeatRepository seatRepository, BalanceHistoryRepository balanceHistoryRepository,
            QueueTokenRepository queueTokenRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.balanceRepository = balanceRepository;
        this.seatRepository = seatRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void confirmReservation(Long userId, Long reservationId) {
        Reservation reservation = validateReservation(userId, reservationId);
        long amount = getSeatPrice(reservation.getSeatId());
        Balance balance = deductBalance(userId, amount);
        confirmReservation(reservation);
        savePayment(userId, reservationId, amount);
        saveBalanceHistory(userId, amount, balance.getBalance());
        expireQueueToken(userId);
    }

    private Reservation validateReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException());
        if (!reservation.isLocked()) {
            throw new NotTemporaryReservationException();
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new NotYourReservationException();
        }
        return reservation;
    }

    private long getSeatPrice(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException());
        long amount = seat.getPrice();
        if (amount <= 0) {
            throw new InvalidSeatPriceException();
        }
        return amount;
    }

    private Balance deductBalance(Long userId, long amount) {
        Balance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new BalanceNotFoundException());
        if (balance.getBalance() < amount) {
            throw new InsufficientBalanceException();
        }
        balance.deduct(amount);
        balanceRepository.save(balance);
        return balance;
    }

    private void confirmReservation(Reservation reservation) {
        reservation.confirm();
        reservationRepository.save(reservation);
    }

    private void savePayment(Long userId, Long reservationId, long amount) {
        Payment payment = Payment.builder()
                .userId(userId)
                .reservationId(reservationId)
                .price(amount)
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);
    }

    private void saveBalanceHistory(Long userId, long amount, long balanceAfter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        BalanceHistory history = BalanceHistory.builder()
                .user(user)
                .type(BalanceHistoryType.USE)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build();
        balanceHistoryRepository.save(history);
    }

    private void expireQueueToken(Long userId) {
        queueTokenRepository.expireTokenByUserId(userId, TokenStatus.EXPIRED, LocalDateTime.now(), TokenStatus.ACTIVE);
    }

}
