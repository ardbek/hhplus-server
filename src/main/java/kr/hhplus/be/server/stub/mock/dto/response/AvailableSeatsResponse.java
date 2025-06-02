package kr.hhplus.be.server.stub.mock.dto.response;

import java.util.List;

/**
 * 예약 가능 좌석 응답 DTO
 * @param concertId - 콘서트 번호
 * @param date - 예약 날짜
 * @param availableSeats - 예약 가능한 좌석 번호 리스트
 */
public record AvailableSeatsResponse(
        String concertId,
        String date,
        List<Integer> availableSeats
) {

}
