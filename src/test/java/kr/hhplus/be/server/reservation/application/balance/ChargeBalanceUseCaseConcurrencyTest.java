package kr.hhplus.be.server.reservation.application.balance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ChargeBalanceUseCaseConcurrencyTest {

    @Autowired private ChargeBalanceUseCase chargeBalanceUseCase;
    @Autowired private BalanceRepository balanceRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 설정
        User user = User.builder().build();
        testUser = userRepository.save(user);

        Balance balance = Balance.builder()
                .balance(0L)
                .userId(testUser.getId())
                .build();
        balanceRepository.save(balance);
    }

    @Test
    void charge_concurrency_test() throws InterruptedException {
        // given
        final int threadCount = 1000;
        final long chargeAmount = 100L;

        // thread pool 설정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    chargeBalanceUseCase.charge(testUser.getId(), chargeAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Balance finalBalance = balanceRepository.findByUserId(testUser.getId()).orElseThrow();
        long expectedAmount = chargeAmount * threadCount;

        // 최종 잔액이 기대값과 일치하는지 확인
        assertThat(finalBalance.getBalance()).isEqualTo(expectedAmount);
        System.out.println("기대 최종 잔액: " + expectedAmount + ", 실제 최종 잔액: " + finalBalance.getBalance());

    }

}
