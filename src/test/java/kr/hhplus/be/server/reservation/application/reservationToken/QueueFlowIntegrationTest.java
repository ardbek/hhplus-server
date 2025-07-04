package kr.hhplus.be.server.reservation.application.reservationToken;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

/**
 * 대기열 진입, 활성화 통합 테스트
 */
@SpringBootTest
public class QueueFlowIntegrationTest {

    @Autowired
    private ReservationTokenRepository tokenRepository;

    @Autowired
    private QueuePromotionScheduler scheduler;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("queue:waiting");
        redisTemplate.delete("queue:processing");
        redisTemplate.delete("queue:active");
    }

    @Test
    void 대기열_진입부터_활성화까지의_전체_흐름_테스트() {
        // given
        // 대기열에 3명의 유저를 직접 추가
        tokenRepository.addWaiting(101L);
        tokenRepository.addWaiting(102L);
        tokenRepository.addWaiting(103L);

        // when
        scheduler.promoteWaitingUsers();

        // then: 워커(Consumer)가 비동기로 처리할 때까지 최대 5초간 기다린 후 검증
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // 3명의 유저가 모두 활성 상태가 되었는지 확인
            assertTrue(tokenRepository.isActiveUser(101L));
            assertTrue(tokenRepository.isActiveUser(102L));
            assertTrue(tokenRepository.isActiveUser(103L));
        });
    }

}
