package kr.hhplus.be.server.reservation.infrastructure.persistence.payment;

import kr.hhplus.be.server.reservation.domain.model.Payment;
import kr.hhplus.be.server.reservation.domain.repository.PaymentRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpa;
    private final UserRepository userRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpa, UserRepository userRepository) {
        this.jpa = jpa;
        this.userRepository = userRepository;
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
        e.user = userRepository.getReferenceById(p.getUserId());
        e.reservationId = p.getReservationId();
        e.price = p.getPrice();
        e.createdAt = p.getCreatedAt();
        return e;
    }

    private Payment toDomain(PaymentEntity e) {
        return Payment.builder()
                .id(e.id)
                .userId(e.user.getId())
                .reservationId(e.reservationId)
                .price(e.price)
                .createdAt(e.createdAt)
                .build();
    }
}