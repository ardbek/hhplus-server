package kr.hhplus.be.server.queue.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    boolean existsByUserIdAndStatus(Long userId, TokenStatus tokenStatus);

    @Query("SELECT MAX(q.position) FROM QueueToken q")
    Optional<Integer> findMaxPosition();

    Optional<QueueToken> findByToken(String token);

    @Modifying
    @Query("update QueueToken q set q.status = :status, q.expiresAt = :expiresAt where q.userId = :userId and q.status = :currentStatus")
    int expireTokenByUserId(
            @Param("userId") Long userId,
            @Param("status") TokenStatus status,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("currentStatus") TokenStatus currentStatus);
}
