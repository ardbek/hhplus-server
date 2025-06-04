package kr.hhplus.be.server.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
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
public class Wallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @Column(name = "balance")
    private Long balance;

    private final Long MAX_CHARGE_AMOUNT = 2_000_000L;
    private final Long MIN_CHARGE_AMOUNT = 0L;


    public Wallet charge(long chargeAmount) {
        if (chargeAmount <= MIN_CHARGE_AMOUNT || chargeAmount > MAX_CHARGE_AMOUNT) {
            throw new InvalidChargeAmountException();
        }

        if (balance == null) {
            balance = 0L;
        }

        this.balance += chargeAmount;

        return this;

    }

    public void deduct(long amount) {
        if (balance < amount) {
            throw new IllegalStateException("잔액 부족");
        }
        this.balance -= amount;
    }
}
