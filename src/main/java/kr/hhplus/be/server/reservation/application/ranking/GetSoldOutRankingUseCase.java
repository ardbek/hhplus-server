package kr.hhplus.be.server.reservation.application.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kr.hhplus.be.server.reservation.domain.model.ConcertRanking;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class GetSoldOutRankingUseCase {

    private final RedisTemplate<String, String> redisTemplate;
    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertScheduleRepository concertScheduleRepository;

    public GetSoldOutRankingUseCase(RedisTemplate<String, String> redisTemplate,
            ConcertJpaRepository concertJpaRepository,
            ConcertScheduleRepository concertScheduleRepository) {
        this.redisTemplate = redisTemplate;
        this.concertJpaRepository = concertJpaRepository;
        this.concertScheduleRepository = concertScheduleRepository;
    }

    private final String RANKING_KEY = "concert:schedule:ranking";

    @Cacheable(value = "rankingCache", key = "'soldOutTop10'")
    @Transactional(readOnly = true)
    public List<ConcertRanking> execute() {
        Set<TypedTuple<String>> rankingTuples = redisTemplate.opsForZSet()
                .rangeWithScores(RANKING_KEY, 0, 9); // 상위 10개

        if (rankingTuples == null || rankingTuples.isEmpty()) {
            log.info("조회된 랭킹 데이터가 없음.");
            return new ArrayList<>();
        }

        List<ConcertRanking> results = new ArrayList<>();
        int currentRank = 1;

        for (TypedTuple<String> tuple : rankingTuples) {
            String member = tuple.getValue();
            double timeTaken = tuple.getScore();

            String[] parts = member.split(":");
            Long concertId = Long.parseLong(parts[1]);
            Long scheduleId = Long.parseLong(parts[3]);

            ConcertEntity concert = concertJpaRepository.findById(concertId)
                    .orElseThrow(() -> new RuntimeException("Concert not found"));
            ConcertSchedule schedule = concertScheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            results.add(ConcertRanking.builder()
                    .rank(currentRank++)
                    .concertTitle(concert.getTitle())
                    .concertSchedule(schedule.getStartAt())
                    .timeTakenSeconds(timeTaken)
                    .build());
        }

        return results;

    }
}
