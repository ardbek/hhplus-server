package kr.hhplus.be.server.wallet.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.hhplus.be.server.wallet.domain.Balance;
import kr.hhplus.be.server.wallet.exception.InvalidChargeAmountException;
import kr.hhplus.be.server.wallet.exception.WalletNotFoundException;
import kr.hhplus.be.server.wallet.repository.BalanceRepository;
import kr.hhplus.be.server.wallet.service.impl.BalanceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @InjectMocks
    private BalanceServiceImpl balanceServiceImpl;

    @Mock
    BalanceRepository balanceRepository;

    private Balance createTestWallet(Long id, Long balance) {
        Balance wallet = Balance.builder().balance(balance).build();
        ReflectionTestUtils.setField(wallet, "id", id);

        return wallet;
    }

    @Test
    @DisplayName("잔액을 정상적으로 조회한다")
    void getUserPoint_success() {
        //given
        Long walletId = 1L;
        Long userId = 1L;
        Long amount = 5_000L;

        Balance balance = createTestWallet(walletId, amount);
        given(balanceRepository.findByUserId(walletId)).willReturn(Optional.of(balance));

        //when
        Balance findBalance = balanceServiceImpl.getBalance(userId);

        //then
        assertThat(findBalance.getId()).isEqualTo(walletId);
        assertThat(findBalance.getBalance()).isEqualTo(amount);

    }

    @Test
    @DisplayName("존재하지 않는 지갑 조회 시 예외를 발생시킨다")
    void getBalance_walletNotFound() {
        // given
        given(balanceRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> balanceServiceImpl.getBalance(1L));

        // then
        assertThat(throwable)
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("지갑을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("잔액을 충전한다.")
    void charge_success() {
        //given
        Long walletId = 1L;
        Long userId = 1L;
        Long chargeAmount = 1_000L;
        Long initialAmount = 5_000L;

        Balance balance = createTestWallet(walletId, initialAmount);

        given(balanceRepository.findByIdForUpdate(walletId)).willReturn(Optional.of(balance));
        given(balanceRepository.save(balance)).willReturn(balance);

        //when
        Balance charged = balanceServiceImpl.charge(walletId, 1_000L);

        //then
        assertThat(charged.getBalance()).isEqualTo(initialAmount + chargeAmount);
        assertThat(charged.getId()).isEqualTo(walletId);

    }

    @Test
    @DisplayName("존재하지 않는 지갑에 충전 요청 시 예외를 발생시킨다.")
    void charge_fail_when_wallet_not_found() {
        // given
        Long walletId = 1L;
        given(balanceRepository.findByIdForUpdate(walletId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> balanceServiceImpl.charge(walletId, 1_000L));

        // then
        assertThat(throwable)
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("지갑을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("잔액을 1원 미만으로 충전할 경우 예외를 발생시킨다")
    void charge_fail_when_charge_amount_less_then_zero() {
        // given
        Long walletId = 1L;
        Long chargeAmount = 0L;

        // when
        Throwable throwable = catchThrowable(() -> balanceServiceImpl.charge(walletId, chargeAmount));

        // then
        assertThat(throwable)
            .isInstanceOf(InvalidChargeAmountException.class)
            .hasMessageContaining("1회 충전 금액은 1원 이상, 2,000,000원 이하만 가능합니다.");
    }

    @Test
    @DisplayName("1회 최대 충전 잔액이 2,000,000원을 초과할 경우 예외를 발생시킨다.")
    void charge_fail_when_charge_amount_more_then_max_price() {
        // given
        Long walletId = 1L;
        Long chargeAmount = 2_000_001L;

        // when
        Throwable throwable = catchThrowable(() -> balanceServiceImpl.charge(walletId, chargeAmount));

        // then
        assertThat(throwable)
            .isInstanceOf(InvalidChargeAmountException.class)
            .hasMessageContaining("1회 충전 금액은 1원 이상, 2,000,000원 이하만 가능합니다.");
    }

}
