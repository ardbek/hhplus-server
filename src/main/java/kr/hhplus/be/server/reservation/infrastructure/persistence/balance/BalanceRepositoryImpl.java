package kr.hhplus.be.server.reservation.infrastructure.persistence.balance; // 또는 wallet.infrastructure

import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BalanceRepositoryImpl implements BalanceRepository {

    private final BalanceJpaRepository jpaRepository;
    private final UserRepository userRepository;

    public BalanceRepositoryImpl(BalanceJpaRepository jpaRepository, UserRepository userRepository) {
        this.jpaRepository = jpaRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Balance> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Balance> findByUserIdForUpdate(Long userId) {
        return jpaRepository.findByUserIdForUpdate(userId)
                .map(this::toDomain);
    }

    @Override
    public Balance save(Balance balance) {
        BalanceEntity entity = toEntity(balance);

        BalanceEntity savedEntity = jpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    /**
     * 도메인 객체를 JPA Entity로 변환하는 private 헬퍼 메소드
     */
    private BalanceEntity toEntity(Balance b) {
        return BalanceEntity.builder()
                .id(b.getId())
                .user(userRepository.getReferenceById(b.getUserId()))
                .balance(b.getBalance())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    /**
     * JPA Entity를 도메인 객체로 변환하는 private 헬퍼 메소드
     */
    private Balance toDomain(BalanceEntity e) {
        return Balance.builder()
                .id(e.getId())
                .userId(e.user.getId())
                .balance(e.getBalance())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}