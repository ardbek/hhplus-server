package kr.hhplus.be.server.config.jpa;

import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.reservation.application.balance.ChargeBalanceUseCase;
import kr.hhplus.be.server.reservation.application.reservation.ExpireReservationUseCase;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableDatesUseCase;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableSeatsUseCase;
import kr.hhplus.be.server.reservation.application.reservationToken.CheckQueueStatusUseCase;
import kr.hhplus.be.server.reservation.application.reservation.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.balance.GetBalanceUseCase;
import kr.hhplus.be.server.reservation.application.reservationToken.IssueReservationTokenUseCase;
import kr.hhplus.be.server.reservation.application.reservationToken.QueueActivationScheduler;
import kr.hhplus.be.server.reservation.application.reservation.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.reservation.ReservationJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ReserveTemporarySeatUseCase reserveSeatUseCase(
            ReservationRepository reservationRepository, SeatRepository seatRepository) {
        return new ReserveTemporarySeatUseCase(reservationRepository, seatRepository);
    }

    @Bean
    public IssueReservationTokenUseCase issueReservationTokenUseCase(
            UserRepository userRepository,
            ReservationTokenRepository reservationTokenRepository
    ) {
        return new IssueReservationTokenUseCase(userRepository, reservationTokenRepository);
    }

    @Bean
    public CheckQueueStatusUseCase checkQueueStatusUseCase(
            ReservationTokenRepository tokenRepository
    ) {
        return new CheckQueueStatusUseCase(tokenRepository);
    }

    @Bean
    public QueueActivationScheduler queueActivationScheduler(
            ReservationTokenRepository tokenRepository) {
        return new QueueActivationScheduler(tokenRepository);
    }

    @Bean
    public ChargeBalanceUseCase chargeBalanceUseCase(BalanceRepository balanceRepository) {
        return new ChargeBalanceUseCase(balanceRepository);
    }

    @Bean
    public GetBalanceUseCase getBalanceUseCase(BalanceRepository balanceRepository) {
        return new GetBalanceUseCase(balanceRepository);
    }

    @Bean
    public GetAvailableDatesUseCase getAvailableDatesUseCase(
        ConcertScheduleJpaRepository concertScheduleJpaRepository) {
        return new GetAvailableDatesUseCase(concertScheduleJpaRepository);
    }

    @Bean
    public GetAvailableSeatsUseCase getAvailableSeatsUseCase(
        ReservationRepository reservationRepository, SeatJpaRepository seatJpaRepository) {
        return new GetAvailableSeatsUseCase(reservationRepository, seatJpaRepository);
    }

    @Bean
    public ExpireReservationUseCase expireReservationUseCase(
            ReservationRepository reservationRepository, SeatRepository seatRepository
    ) {
        return new ExpireReservationUseCase(reservationRepository, seatRepository);
    }


    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            BalanceRepository balanceRepository,
            SeatJpaRepository seatJpaRepository,
            BalanceHistoryRepository balanceHistoryRepository,
            QueueTokenRepository queueTokenRepository,
            UserRepository userRepository
    ) {
        return new ConfirmPaymentUseCase(
                reservationRepository,
                paymentRepository,
                balanceRepository,
            seatJpaRepository,
                balanceHistoryRepository,
                queueTokenRepository,
                userRepository
        );
    }

}
