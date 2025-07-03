package kr.hhplus.be.server.reservation.application.reservationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class ReservationTokenConCurrencyTest {

    @Container
    private static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
    }

    @Autowired
    private IssueReservationTokenUseCase issueReservationTokenUseCase;

    @Autowired
    private ReservationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private QueuePromotionScheduler queuePromotionScheduler;

    @MockitoBean
    private QueueActivationWorker queueActivationWorker;

    @MockitoBean
    private WorkerStarter workerStarter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("queue:waiting");
        for (int i = 0; i < 100; i++) {
            userRepository.save(User.builder().build());
        }
    }

    @Test
    @DisplayName("100명의 사용자가 동시에 대기열 진입을 요청했을 때 모두 정상적으로 추가된다.")
    void 동시_100명_대기열_진입_요청_테스트() throws InterruptedException {
        int userCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(userCount);

        for (int i = 0; i < userCount; i++) {
            final long userId = i + 1;
            executorService.submit(() -> {
                try {
                    issueReservationTokenUseCase.issueReservationToken(userId);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Long waitingQueueSize = tokenRepository.getWaitingQueueSize();
        assertEquals(userCount, waitingQueueSize);
    }
}
