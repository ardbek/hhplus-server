package kr.hhplus.be.server.config.jpa;

import kr.hhplus.be.server.reservation.application.ConfirmPaymentUseCase;
import kr.hhplus.be.server.reservation.application.ReserveSeatUseCase;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ReserveSeatUseCase reserveSeatUseCase(ReservationRepository reservationRepository) {
        return new ReserveSeatUseCase(reservationRepository);
    }

    @Bean
    public ConfirmPaymentUseCase confirmPaymentUseCase(ReservationRepository reservationRepository) {
        return new ConfirmPaymentUseCase(reservationRepository);
    }
}
