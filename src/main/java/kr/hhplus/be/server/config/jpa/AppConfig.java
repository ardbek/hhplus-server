package kr.hhplus.be.server.config.jpa;

import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.reservation.application.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
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
    public ConfirmPaymentUseCase confirmPaymentUseCase(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            BalanceRepository balanceRepository,
            SeatRepository seatRepository,
            BalanceHistoryRepository balanceHistoryRepository,
            QueueTokenRepository queueTokenRepository,
            UserRepository userRepository
    ) {
        return new ConfirmPaymentUseCase(
                reservationRepository,
                paymentRepository,
                balanceRepository,
                seatRepository,
                balanceHistoryRepository,
                queueTokenRepository,
                userRepository
        );
    }

}
