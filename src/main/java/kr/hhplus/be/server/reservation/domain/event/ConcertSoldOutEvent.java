package kr.hhplus.be.server.reservation.domain.event;

/**
 * 랭킹 기록을 위한 콘서트 매진 이벤트
 * @param concertId
 * @param concertScheduleId
 */
public record ConcertSoldOutEvent(
        Long concertId,
        Long concertScheduleId
) {

}

