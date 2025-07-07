package kr.hhplus.be.server.reservation.application.reservation;


public class ConcertSoldOutEvent {

    private final Long concertId;
    private final Long concertScheduleId;

    public ConcertSoldOutEvent(Long concertId, Long concertScheduleId) {
        this.concertId = concertId;
        this.concertScheduleId = concertScheduleId;
    }

    public Long getConcertId() {
        return concertId;
    }

    public Long getConcertScheduleId() {
        return concertScheduleId;
    }
}
