package kr.hhplus.be.server.reservation.interfaces.web.dto.request.reservation;

/**
 * 토큰 발급 요청 DTO
 * @param userId - 유저 번호
 */
public record ReservationTokenIssueRequest(
        Long userId
) {
}
