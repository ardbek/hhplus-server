package kr.hhplus.be.server.reservation.application.balance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;
import kr.hhplus.be.server.queue.exception.balance.InvalidChargeAmountException;
import kr.hhplus.be.server.queue.exception.balance.WalletNotFoundException;
import kr.hhplus.be.server.reservation.domain.model.Balance;
import kr.hhplus.be.server.reservation.domain.repository.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChargeBalanceUseCaseTest {

    @InjectMocks
    private ChargeBalanceUseCase chargeBalanceUseCase;

    @Mock
    private BalanceRepository balanceRepository;

    @Test
    @DisplayName("잔액 충전에 성공한다.")
    void charge_success() {
        // given
        Long userId = 1L;
        Long initialAmount = 10_000L;
        Long chargeAmount = 5_000L;

        Balance userBalance = Balance.builder()
                .id(1L)
                .userId(userId)
                .balance(initialAmount)
                .build();

        given(balanceRepository.findByUserIdForUpdate(userId)).willReturn(Optional.of(userBalance));
        given(balanceRepository.save(userBalance)).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Balance result = chargeBalanceUseCase.charge(userId, chargeAmount);

        // then
        assertThat(result.getBalance()).isEqualTo(initialAmount + chargeAmount);
    }

    @Test
    @DisplayName("잔고 정보가 없을 경우 예외가 발생한다.")
    void charge_fail_when_wallet_not_found() {
        // given
        Long nonExistentUserId = 999L;
        Long chargeAmount = 5_000L;

        given(balanceRepository.findByUserIdForUpdate(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chargeBalanceUseCase.charge(nonExistentUserId, chargeAmount))
                .isInstanceOf(WalletNotFoundException.class);

        then(balanceRepository).should(never()).save(any(Balance.class));
    }

    @Test
    @DisplayName("유효하지 않은 금액으로 충전 시도 시 예외가 발생한다.")
    void charge_fail_with_invalid_amount1() {
        // given
        Long userId = 1L;
        Long initialAmount = 10_000L;
        Long invalidChargeAmount = -100L;

        Balance userBalance = Balance.builder()
                .id(1L)
                .userId(userId)
                .balance(initialAmount)
                .build();

        given(balanceRepository.findByUserIdForUpdate(userId)).willReturn(Optional.of(userBalance));

        // when & then
        assertThatThrownBy(() -> chargeBalanceUseCase.charge(userId, invalidChargeAmount))
                .isInstanceOf(InvalidChargeAmountException.class);

        then(balanceRepository).should(never()).save(any(Balance.class));
    }

    @Test
    @DisplayName("1회 최대 충전 금액이 2,000,000을 넘길 경우 예외가 발생한다.")
    void charge_fail_with_invalid_amount2() {
        // given
        Long userId = 1L;
        Long initialAmount = 10_000L;
        Long invalidChargeAmount = 2_000_001L;

        Balance userBalance = Balance.builder()
                .id(1L)
                .userId(userId)
                .balance(initialAmount)
                .build();

        given(balanceRepository.findByUserIdForUpdate(userId)).willReturn(Optional.of(userBalance));

        // when & then
        assertThatThrownBy(() -> chargeBalanceUseCase.charge(userId, invalidChargeAmount))
                .isInstanceOf(InvalidChargeAmountException.class);

        then(balanceRepository).should(never()).save(any(Balance.class));
    }

}
