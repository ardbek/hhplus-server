package kr.hhplus.be.server.wallet.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.wallet.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    Optional<Balance> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Balance w WHERE w.id = :walletId")
    Optional<Balance> findByIdForUpdate(@Param("walletId") Long walletId);
}
