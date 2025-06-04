package kr.hhplus.be.server.reservation.infrastructure.persistence;

import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpa;

    public PaymentRepositoryImpl(PaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity e = toEntity(payment);
        PaymentEntity saved = jpa.save(e);
        return toDomain(saved);
    }

    private PaymentEntity toEntity(Payment p) {
        PaymentEntity e = new PaymentEntity();
        e.id = p.getId();
        e.userId = p.getUserId();
        e.reservationId = p.getReservationId();
        e.price = p.getPrice();
        e.createdAt = p.getCreatedAt();
        return e;
    }

    private Payment toDomain(PaymentEntity e) {
        return Payment.builder()
                .id(e.id)
                .userId(e.userId)
                .reservationId(e.reservationId)
                .price(e.price)
                .createdAt(e.createdAt)
                .build();
    }
}