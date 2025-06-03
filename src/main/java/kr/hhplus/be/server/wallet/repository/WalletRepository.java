package kr.hhplus.be.server.wallet.repository;

import java.util.Optional;
import kr.hhplus.be.server.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
}
