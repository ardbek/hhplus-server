package kr.hhplus.be.server.reservation.infrastructure.persistence.balance;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BalanceJpaRepository extends JpaRepository<BalanceEntity, Long> {

    Optional<BalanceEntity> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM BalanceEntity w WHERE w.id = :userId")
    Optional<BalanceEntity> findByIdForUpdate(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BalanceEntity b WHERE b.user.id = :userId")
    Optional<BalanceEntity> findByUserIdForUpdate(@Param("userId") Long userId);
}
