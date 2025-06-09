package kr.hhplus.be.server.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import kr.hhplus.be.server.reservation.exception.InsufficientBalanceException;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.wallet.exception.InvalidChargeAmountException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Balance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "balance")
    private Long balance;

    private final Long MAX_CHARGE_AMOUNT = 2_000_000L;
    private final Long MIN_CHARGE_AMOUNT = 0L;


    public Balance charge(long chargeAmount) {
        if (chargeAmount <= MIN_CHARGE_AMOUNT || chargeAmount > MAX_CHARGE_AMOUNT) {
            throw new InvalidChargeAmountException();
        }

        if (balance == null) {
            balance = 0L;
        }

        this.balance += chargeAmount;

        return this;

    }

    /**
     * 결제를 위해 금액을 차감합니다.
     * @param amount 결제할 금액
     * @throws InsufficientBalanceException 잔액이 부족할 경우
     */
    public void pay(long amount) {
        if (balance < amount) {
            throw new InsufficientBalanceException();
        }
        this.balance -= amount;
    }
}
