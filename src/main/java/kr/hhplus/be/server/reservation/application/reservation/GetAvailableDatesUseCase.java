package kr.hhplus.be.server.reservation.application.reservation;

import java.time.LocalDate;
import java.util.List;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;

public class GetAvailableDatesUseCase {

    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;

    public GetAvailableDatesUseCase(ConcertScheduleJpaRepository concertScheduleJpaRepository) {
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
    }

    public List<LocalDate> getAvailableDates(Long concertId) {
        return concertScheduleJpaRepository.findByConcertId(concertId).stream()
            .map(schedule -> schedule.getStartAt().toLocalDate())
            .distinct()
            .sorted()
            .toList();
    }
}
