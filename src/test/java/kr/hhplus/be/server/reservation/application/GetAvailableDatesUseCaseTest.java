package kr.hhplus.be.server.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.reservation.application.reservation.GetAvailableDatesUseCase;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAvailableDatesUseCaseTest {

    @InjectMocks
    private GetAvailableDatesUseCase getAvailableDatesUseCase;

    @Mock
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    private ConcertScheduleEntity createTestSchedule(Long scheduleId, LocalDateTime startAt) {
        return ConcertScheduleEntity.builder()
            .id(scheduleId)
            .concert(null)
            .startAt(startAt)
            .build();
    }

    @Test
    @DisplayName("예약 가능 날짜를 정상적으로 조회한다.")
    void getAvailableDates_success() {
        // given
        Long concertId = 1L;
        LocalDateTime concertDate1 = LocalDateTime.of(2025, 6, 10, 18, 0);
        LocalDateTime concertDate2 = LocalDateTime.of(2025, 6, 11, 18, 0);
        LocalDateTime concertDate3 = LocalDateTime.of(2025, 6, 12, 18, 0);
        LocalDateTime concertDate4 = LocalDateTime.of(2025, 6, 12, 19, 0);

        List<ConcertScheduleEntity> schedules = List.of(
            createTestSchedule(1L, concertDate1),
            createTestSchedule(3L, concertDate3),
            createTestSchedule(2L, concertDate2),
            createTestSchedule(4L, concertDate4)
        );

        given(concertScheduleJpaRepository.findByConcertId(concertId)).willReturn(schedules);

        // when
        List<LocalDate> availableDates = getAvailableDatesUseCase.getAvailableDates(concertId);

        // then
        assertThat(availableDates).containsExactly(
            LocalDate.of(2025, 6, 10),
            LocalDate.of(2025, 6, 11),
            LocalDate.of(2025, 6, 12)
        );
    }
}