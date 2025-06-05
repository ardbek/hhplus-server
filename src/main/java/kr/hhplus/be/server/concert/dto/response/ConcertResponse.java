package kr.hhplus.be.server.concert.dto.response;

import java.util.List;

public record ConcertResponse(
        List<ConcertData> concerts
) {

}
