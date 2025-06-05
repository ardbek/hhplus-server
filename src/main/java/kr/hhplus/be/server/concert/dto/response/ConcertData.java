package kr.hhplus.be.server.concert.dto.response;

import kr.hhplus.be.server.concert.domain.Concert;

public record ConcertData(
        long concertId,
        String title
) {

    public static ConcertData from(Concert concert) {
        return new ConcertData(concert.getId(), concert.getTitle());
    }

}
