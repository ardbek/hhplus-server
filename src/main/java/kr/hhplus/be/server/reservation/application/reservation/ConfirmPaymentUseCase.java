package kr.hhplus.be.server.reservation.application.reservation;

import java.time.LocalDateTime;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import kr.hhplus.be.server.balanceHistory.domain.BalanceHistoryType;
import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.exception.balance.BalanceNotFoundException;
import kr.hhplus.be.server.reservation.exception.concertSchedule.ConcertScheduleNotFoundException;
import kr.hhplus.be.server.reservation.exception.reservation.ReservationNotFoundException;
import kr.hhplus.be.server.reservation.exception.seat.SeatNotFoundException;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;

public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationTokenRepository reservationTokenRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceRepository balanceRepository;
    private final SeatRepository seatRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;

    public ConfirmPaymentUseCase(ReservationRepository reservationRepository,
            ReservationTokenRepository reservationTokenRepository,
            ConcertScheduleRepository concertScheduleRepository,
            PaymentRepository paymentRepository, BalanceRepository balanceRepository,
            SeatRepository seatRepository, BalanceHistoryRepository balanceHistoryRepository,
            UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.reservationTokenRepository = reservationTokenRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.paymentRepository = paymentRepository;
        this.balanceRepository = balanceRepository;
        this.seatRepository = seatRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @DistributedLock(key="'seat:'+#seatId")
    public void confirmReservation(Long userId, Long reservationId,Long seatId) {
        // 1. 도메인 객체 조회
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        Seat seatEntity = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
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

        // 5. 매진 여부 확인
        if (isSoldOut(reservation.getConcertScheduleId())) {
            ConcertSchedule schedule = concertScheduleRepository.findById(reservation.getConcertScheduleId())
                    .orElseThrow(ConcertScheduleNotFoundException::new);

            // 조회한 schedule에서 concertId를 가져와 이벤트 생성
            ConcertSoldOutEvent event = new ConcertSoldOutEvent(
                    schedule.getConcertId(),
                    reservation.getConcertScheduleId()
            );
            eventPublisher.publishEvent(event);
        }


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

    /**
     * 매진 판별 여부
     * @return
     */
    private boolean isSoldOut(Long scheduleId) {
        long totalSeats = seatRepository.countByConcertScheduleEntity_Id(scheduleId);
        long confirmedReservations = reservationRepository.countByConcertScheduleEntity_IdAndStatus(scheduleId, ReservationStatus.CONFIRMED);
        return totalSeats > 0 && totalSeats == confirmedReservations;
    }

}
