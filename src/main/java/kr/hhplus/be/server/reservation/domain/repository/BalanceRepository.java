package kr.hhplus.be.server.reservation.domain.repository;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.Balance;

public interface BalanceRepository {

    Optional<Balance> findByUserId(Long userId);

    Optional<Balance> findByUserIdForUpdate(Long userId);

    Balance save(Balance balance);

}
