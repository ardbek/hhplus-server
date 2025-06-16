package kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert;

import kr.hhplus.be.server.reservation.domain.model.Concert;

public record ConcertData(
        long concertId,
        String title
) {
    public static ConcertData from(Concert concert) {
        return new ConcertData(concert.getId(), concert.getTitle());
    }
} 