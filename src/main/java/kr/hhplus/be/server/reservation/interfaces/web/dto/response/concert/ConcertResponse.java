package kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert;

import java.util.List;

public record ConcertResponse(
        List<ConcertData> concerts
) {
} 