package kr.hhplus.be.server.queue.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    boolean existsByUserIdAndStatus(Long userId, TokenStatus tokenStatus);

    Optional<QueueToken> findByToken(String token);

    @Modifying
    @Query("UPDATE QueueToken q SET q.status = :status, q.expiresAt = :expiresAt WHERE q.user.id = :userId AND q.status = :currentStatus")
    int expireTokenByUserId(
            @Param("userId") Long userId,
            @Param("status") TokenStatus status,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("currentStatus") TokenStatus currentStatus);

    @Query("SELECT COUNT(*) FROM QueueToken WHERE status = :status AND createdAt < :issuedAt")
    int countByStatusAndCreatedAtBefore(@Param("status") TokenStatus tokenStatus, @Param("issuedAt") LocalDateTime issuedAt);

    boolean existsByUserAndStatus(User user, TokenStatus tokenStatus);
}
