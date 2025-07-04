package kr.hhplus.be.server.reservation.application.reservation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
public class RecordSoldOutUseCaseTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @InjectMocks
    private RecordSoldOutUseCase recordSoldOutUseCase;

    @Test
    @DisplayName("콘서트 매진 시 올바른 값으로 Redis에 저장되어야 한다.")
    void record_sold_out_test() {
        // given
        Long concertId = 1L;
        Long scheduleId = 10L;
        LocalDateTime ticketOpenTime = LocalDateTime.now().minusSeconds(30);

        ConcertSchedule mockSchedule = ConcertSchedule.builder()
                .id(scheduleId)
                .concertId(concertId)
                .ticketOpenTime(ticketOpenTime)
                .build();

        when(concertScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(mockSchedule));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // when
        recordSoldOutUseCase.record(concertId, scheduleId);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> memberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

        verify(zSetOperations, times(1)).add(keyCaptor.capture(), memberCaptor.capture(), scoreCaptor.capture());

        assertEquals("concert:schedule:ranking", keyCaptor.getValue());
        assertEquals("concert:" + concertId + ":schedule:" + scheduleId, memberCaptor.getValue());
        assertTrue(scoreCaptor.getValue() >= 30.0); // 점수(걸린 시간)가 30초 이상인지 확인

    }
}
