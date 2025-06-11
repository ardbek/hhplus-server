package kr.hhplus.be.server.reservation.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.hhplus.be.server.reservation.application.balance.GetBalanceUseCase;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import kr.hhplus.be.server.reservation.exception.balance.BalanceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetBalanceUseCaseTest {
    @InjectMocks
    private GetBalanceUseCase getBalanceUseCase;

    @Mock
    private BalanceRepository balanceRepository;

    @Test
    @DisplayName("성공 - 사용자 ID로 잔액을 성공적으로 조회한다")
    void getBalance_success() {
        // given
        Long userId = 1L;
        Balance mockBalance = Balance.builder()
                .id(10L)
                .userId(userId)
                .balance(5000L)
                .build();

        given(balanceRepository.findByUserId(userId)).willReturn(Optional.of(mockBalance));

        // when
        Balance resultBalance = getBalanceUseCase.getBalance(userId);

        // then
        assertThat(resultBalance).isNotNull();
        assertThat(resultBalance.getUserId()).isEqualTo(userId);
        assertThat(resultBalance.getBalance()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("실패 - 잔액 정보가 존재하지 않는 사용자")
    void getBalance_fail_balanceNotFound() {
        // given
        Long userId = 999L;

        given(balanceRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(BalanceNotFoundException.class, () -> {
            getBalanceUseCase.getBalance(userId);
        });
    }
}