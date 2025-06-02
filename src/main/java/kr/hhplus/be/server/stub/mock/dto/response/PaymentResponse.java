package kr.hhplus.be.server.stub.mock.dto.response;

public record PaymentResponse(
        String paymentId,
        String userId,
        String seatId,
        String status,
        boolean tokenExpired
) {

}
