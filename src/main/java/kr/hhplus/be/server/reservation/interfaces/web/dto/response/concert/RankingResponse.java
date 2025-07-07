package kr.hhplus.be.server.reservation.interfaces.web.dto.response.concert;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RankingResponse {
    private final int rank;
    private final String concertTitle;
    private final LocalDateTime concertSchedule;
    private final double timeTakenSeconds;

    @Builder
    public RankingResponse(int rank, String concertTitle, LocalDateTime concertSchedule, double timeTakenSeconds) {
        this.rank = rank;
        this.concertTitle = concertTitle;
        this.concertSchedule = concertSchedule;
        this.timeTakenSeconds = timeTakenSeconds;
    }
}