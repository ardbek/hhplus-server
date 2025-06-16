package kr.hhplus.be.server.reservation.application.concert;

import kr.hhplus.be.server.reservation.domain.model.Concert;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetConcertsUseCase {
    private final ConcertJpaRepository concertJpaRepository;

    @Transactional(readOnly = true)
    public List<Concert> getConcerts() {
        return concertJpaRepository.findAll().stream()
                .map(entity -> Concert.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .build())
                .toList();
    }
} 