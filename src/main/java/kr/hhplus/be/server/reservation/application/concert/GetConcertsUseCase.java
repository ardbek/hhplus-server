package kr.hhplus.be.server.reservation.application.concert;

import kr.hhplus.be.server.reservation.domain.model.Concert;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
public class GetConcertsUseCase {
    private final ConcertJpaRepository concertJpaRepository;

    public GetConcertsUseCase(ConcertJpaRepository concertJpaRepository) {
        this.concertJpaRepository = concertJpaRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value="concerts", sync = true)
    public List<Concert> getConcerts() {
        log.info("[Cache Miss] Fetching concerts from DB.. ");
        return concertJpaRepository.findAll().stream()
                .map(entity -> Concert.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .build())
                .toList();
    }
} 