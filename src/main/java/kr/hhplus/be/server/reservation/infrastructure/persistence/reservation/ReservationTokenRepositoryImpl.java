package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.util.Set;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationTokenRepositoryImpl implements ReservationTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String WAITING_QUEUE_KEY = "queue:waiting";
    private static final String ACTIVE_SET_KEY = "queue:active";
    private static final String TOKEN_PREFIX = "token:";

    private String getTokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    private String getActiveUserKey(Long userId) {
        return ACTIVE_SET_KEY + ":" + userId;
    }

    @Override
    public void saveToken(String token, Long userId) {
        redisTemplate.opsForValue().set(getTokenKey(token), String.valueOf(userId));
    }

    @Override
    public Long getUserIdByToken(String token) {
        String userIdStr = redisTemplate.opsForValue().get(getTokenKey(token));
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    @Override
    public void addWaiting(Long userId) {
        long timestamp = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, String.valueOf(userId), timestamp);
    }

    @Override
    public Long getRank(Long userId) {
        return redisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, String.valueOf(userId));
    }

    @Override
    public Set<Long> getTopRankedUsers(long count) {
        Set<String> userIds = redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, 0, count - 1);
        if (userIds == null) {
            return Set.of();
        }
        return userIds.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

    @Override
    public void removeFromWaiting(Long userId) {
        redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, String.valueOf(userId));
    }

    @Override
    public void setActiveUser(Long userId) {
        redisTemplate.opsForValue().set(getActiveUserKey(userId), "active");
    }

    @Override
    public boolean isActiveUser(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getActiveUserKey(userId)));
    }
}
