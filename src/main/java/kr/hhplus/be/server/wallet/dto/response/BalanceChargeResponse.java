package kr.hhplus.be.server.wallet.dto.response;

public record BalanceChargeResponse(
        long walletId,
        long balance
) {

}
