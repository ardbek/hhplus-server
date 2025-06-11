package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;
import kr.hhplus.be.server.queue.exception.balance.InvalidChargeAmountException;
import kr.hhplus.be.server.reservation.exception.balance.InsufficientBalanceException;

public class Balance {

    private Long id;
    private Long userId;
    private Long balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final Long MAX_CHARGE_AMOUNT = 2_000_000L;
    private final Long MIN_CHARGE_AMOUNT = 0L;

    private Balance(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.balance = builder.balance;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder(){
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private Long balance;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder balance(Long balance) {
            this.balance = balance;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Balance build() {
            return new Balance(this);
        }
    }

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
