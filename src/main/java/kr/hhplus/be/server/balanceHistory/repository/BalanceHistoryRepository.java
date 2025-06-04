package kr.hhplus.be.server.balanceHistory.repository;

import kr.hhplus.be.server.balanceHistory.domain.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Long> {

}
