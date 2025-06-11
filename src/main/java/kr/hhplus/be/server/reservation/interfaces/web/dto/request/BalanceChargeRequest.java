package kr.hhplus.be.server.reservation.interfaces.web.dto.request;

public record BalanceChargeRequest(
    Long walletId,
    Long chargeAmount
) {

}
