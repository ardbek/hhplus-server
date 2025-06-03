package kr.hhplus.be.server.queue.repository;

import java.util.Optional;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    boolean existsByUserIdAndStatus(Long userId, TokenStatus tokenStatus);

    @Query("SELECT MAX(q.position) FROM QueueToken q")
    Optional<Integer> findMaxPosition();

    Optional<QueueToken> findByToken(String token);
}
