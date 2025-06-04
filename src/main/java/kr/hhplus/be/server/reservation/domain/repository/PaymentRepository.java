package kr.hhplus.be.server.reservation.domain.repository;

import kr.hhplus.be.server.reservation.domain.model.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);
}
