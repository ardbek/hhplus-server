package kr.hhplus.be.server.config.jpa;

import kr.hhplus.be.server.balanceHistory.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.queue.repository.QueueTokenRepository;
import kr.hhplus.be.server.reservation.application.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.ReserveTemporarySeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import kr.hhplus.be.server.wallet.repository.WalletRepository;
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
    public ConfirmPaymentUseCase confirmPaymentUseCase(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            WalletRepository walletRepository,
            SeatRepository seatRepository,
            BalanceHistoryRepository balanceHistoryRepository,
            QueueTokenRepository queueTokenRepository,
            UserRepository userRepository
    ) {
        return new ConfirmPaymentUseCase(
                reservationRepository,
                paymentRepository,
                walletRepository,
                seatRepository,
                balanceHistoryRepository,
                queueTokenRepository,
                userRepository
        );
    }

}
