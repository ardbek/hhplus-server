package kr.hhplus.be.server.reservation.exception.concertSchedule;

public class ConcertScheduleNotFoundException extends RuntimeException {

    public ConcertScheduleNotFoundException() {
        super("콘서트 일정이 존재하지 않습니다.");
    }
}
