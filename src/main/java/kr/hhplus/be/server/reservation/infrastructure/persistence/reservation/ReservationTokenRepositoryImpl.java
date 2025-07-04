package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationTokenRepositoryImpl implements ReservationTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String WAITING_QUEUE_KEY = "queue:waiting";
    private static final String ACTIVE_SET_KEY = "queue:active";
    private static final String TOKEN_PREFIX = "token:";

    private static final String PROCESSING_QUEUE_KEY = "queue:processing";

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
        redisTemplate.opsForSet().add(ACTIVE_SET_KEY,String.valueOf(userId));
    }

    @Override
    public void removeActiveUser(Long userId) {
        redisTemplate.opsForSet().remove(ACTIVE_SET_KEY, String.valueOf(userId));
    }

    @Override
    public boolean isActiveUser(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_SET_KEY, String.valueOf(userId)));
    }

    /**
     * 처리 큐에 userId 추가
     * @param userId
     */
    @Override
    public void addProcessingQueue(Long userId) {
        RBlockingQueue<Long> queue = redissonClient.getBlockingQueue(PROCESSING_QUEUE_KEY);
        queue.add(userId);
    }

    /**
     * 큐에서 작업을 가져옴 (Consumer가 사용)
     * 큐가 비어있을 경우, 지정 시간 동안 대기
     * @return userID, 타임아웃 시 null
     */
    @Override
    public Long getFromProcessingQueue() throws InterruptedException {
        RBlockingQueue<Long> queue = redissonClient.getBlockingQueue(PROCESSING_QUEUE_KEY);
        return queue.take(); // 큐에 아이템이 없으면 있을 때까지 대기 (Blocking)
    }

    /**
     * 활성화된 유저 수 조회
     * @return
     */
    @Override
    public Long getActiveUserCount() {
        return redisTemplate.opsForSet().size(ACTIVE_SET_KEY);
    }

    /**
     * ZCARD 명령을 사용하여 waiting_queue의 크기를 조회합니다.
     * @return 대기열의 총 인원
     */
    @Override
    public Long getWaitingQueueSize() {
        return redisTemplate.opsForZSet().zCard(WAITING_QUEUE_KEY);
    }
}
