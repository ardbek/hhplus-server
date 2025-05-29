package kr.hhplus.be.server.stub.mock.dto.response;

import java.util.List;

/**
 * 예약 가능 날짜 조회 응답 DTO
 * @param concertId - 조회할 콘서트 번호
 * @param availableDates - 예약 가능한 날짜 목록
 */
public record AvailableDatesResponse(
        String concertId,
        List<String> availableDates
) {

}
