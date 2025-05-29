package kr.hhplus.be.server.stub.mock.dto.response;

public record ReserveResponse(
    boolean success,
    String tokenExpired,
    String seatId,
    String reserveDate

) {

}
