package kr.hhplus.be.server.reservation.domain.repository;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.Balance;

public interface BalanceRepository {

    // 단순 조회
    Optional<Balance> findByUserId(Long userId);

    // 데이터 변경(x-lock)
    Optional<Balance> findByUserIdForUpdate(Long userId);

    Balance save(Balance balance);

}
