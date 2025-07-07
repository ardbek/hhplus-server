package kr.hhplus.be.server.reservation.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;

public class ConcertRanking {
    private final int rank;
    private final String concertTitle;
    private final LocalDateTime concertSchedule;
    private final double timeTakenSeconds;

    @Builder
    public ConcertRanking(int rank, String concertTitle, LocalDateTime concertSchedule, double timeTakenSeconds) {
        this.rank = rank;
        this.concertTitle = concertTitle;
        this.concertSchedule = concertSchedule;
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public int getRank() {
        return rank;
    }

    public String getConcertTitle() {
        return concertTitle;
    }

    public LocalDateTime getConcertSchedule() {
        return concertSchedule;
    }

    public double getTimeTakenSeconds() {
        return timeTakenSeconds;
    }
}