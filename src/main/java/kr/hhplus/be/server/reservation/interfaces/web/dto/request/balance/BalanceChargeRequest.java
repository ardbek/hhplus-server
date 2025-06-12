package kr.hhplus.be.server.reservation.interfaces.web.dto.request.balance;

public record BalanceChargeRequest(
    Long walletId,
    Long chargeAmount
) {

}
