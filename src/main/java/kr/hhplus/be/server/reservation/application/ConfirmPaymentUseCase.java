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
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.repository.WalletRepository;
import org.springframework.transaction.annotation.Transactional;


public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final SeatRepository seatRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final QueueTokenRepository queueTokenRepository;

    public ConfirmPaymentUseCase(ReservationRepository reservationRepository,
            PaymentRepository paymentRepository, WalletRepository walletRepository,
            SeatRepository seatRepository, BalanceHistoryRepository balanceHistoryRepository,
            QueueTokenRepository queueTokenRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
        this.seatRepository = seatRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.queueTokenRepository = queueTokenRepository;
    }

    @Transactional
    public void confirm(Long userId, Long reservationId) {
        Reservation reservation = validateReservation(userId, reservationId);
        long amount = getSeatPrice(reservation.getSeatId());
        Wallet wallet = deductBalance(userId, amount);
        confirmReservation(reservation);
        savePayment(userId, reservationId, amount);
        saveBalanceHistory(userId, amount, wallet.getBalance());
        expireQueueToken(userId);
    }

    private Reservation validateReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않음"));
        if (!reservation.isLocked()) {
            throw new IllegalStateException("임시예약 상태가 아닙니다.");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 예약이 아닙니다.");
        }
        return reservation;
    }

    private long getSeatPrice(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보 없음"));
        long amount = seat.getPrice();
        if (amount <= 0) {
            throw new IllegalArgumentException("좌석 가격 오류");
        }
        return amount;
    }

    private Wallet deductBalance(Long userId, long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("잔액 정보 없음"));
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("잔액 부족");
        }
        wallet.deduct(amount);
        walletRepository.save(wallet);
        return wallet;
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
        BalanceHistory history = BalanceHistory.builder()
                .userId(userId)
                .type(BalanceHistoryType.USE)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build();
        balanceHistoryRepository.save(history);
    }

    private void expireQueueToken(Long userId) {
        queueTokenRepository.expireTokenByUserId(userId, TokenStatus.EXPIRED, LocalDateTime.now(), TokenStatus.EXPIRED);
    }

}
