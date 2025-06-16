package kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.model.ConcertSchedule;
import kr.hhplus.be.server.reservation.domain.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concert.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepository {

    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;
    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        ConcertScheduleEntity entity = toEntity(concertSchedule);
        ConcertScheduleEntity savedEntity = concertScheduleJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<ConcertSchedule> findById(Long id) {
        return concertScheduleJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ConcertSchedule> findByConcertId(Long concertId) {
        return concertScheduleJpaRepository.findByConcertId(concertId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ConcertScheduleEntity toEntity(ConcertSchedule domain) {
        return ConcertScheduleEntity.builder()
                .id(domain.getId())
                .startAt(domain.getStartAt())
                .concert(concertJpaRepository.getReferenceById(domain.getConcertId()))
                .build();
    }

    private ConcertSchedule toDomain(ConcertScheduleEntity entity) {
        return ConcertSchedule.builder()
                .id(entity.getId())
                .startAt(entity.getStartAt())
                .concertId(entity.getConcert().getId())
                .build();
    }
}