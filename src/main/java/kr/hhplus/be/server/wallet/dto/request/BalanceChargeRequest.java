package kr.hhplus.be.server.wallet.dto.request;

public record BalanceChargeRequest(
    Long walletId,
    Long chargeAmount
) {

}
