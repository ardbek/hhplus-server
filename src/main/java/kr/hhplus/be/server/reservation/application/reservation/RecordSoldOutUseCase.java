package kr.hhplus.be.server.reservation.application.reservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.exception.concertSchedule.ConcertScheduleNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 콘서트 매진 시 랭킹을 위해 기록하는 UseCase
 */
@Slf4j
public class RecordSoldOutUseCase {

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcertScheduleRepository concertScheduleRepository;

    public RecordSoldOutUseCase(RedisTemplate<String, String> redisTemplate,
            ConcertScheduleRepository concertScheduleRepository) {
        this.redisTemplate = redisTemplate;
        this.concertScheduleRepository = concertScheduleRepository;
    }

    public void record(Long concertId, Long concertScheduleId) {

        // 1. 콘서트 회차 정보 조회(판매 시작 시간을 위해)
        ConcertSchedule schedule = concertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(ConcertScheduleNotFoundException::new);

        // 2. 매진까지 걸린 시간 계산
        LocalDateTime ticketOpenTime = schedule.getTicketOpenTime();
        double timeTakenInSeconds = calcSoldOutTime(ticketOpenTime);

        // 3. Redis Sorted Set에 저장
        String rankingKey = "concert:schedule:ranking";
        String member = "concert:" + concertId + ":schedule:" + concertScheduleId;

        Boolean isAdded = redisTemplate.opsForZSet().add(rankingKey, member, timeTakenInSeconds);

        if (isAdded != null && isAdded) {
            Long size = redisTemplate.opsForZSet().size(rankingKey);
            if (size != null && size == 1) {
                redisTemplate.expire(rankingKey, 90, TimeUnit.DAYS);
                log.info("랭킹 키 '{}'에 90일 TTL을 설정", rankingKey);
            }
        }

        redisTemplate.opsForZSet().add(rankingKey, member, timeTakenInSeconds);
        log.info("매진 기록 : {} , 걸린 시간 : {} 초", member, timeTakenInSeconds);

    }

    /**
     * 티켓 매진 시간 계산
     * @param openTime - 티켓 판매 시작 시간
     * @return
     */
    private double calcSoldOutTime(LocalDateTime openTime) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(openTime, now).toMillis() / 1000.0;
    }
}
