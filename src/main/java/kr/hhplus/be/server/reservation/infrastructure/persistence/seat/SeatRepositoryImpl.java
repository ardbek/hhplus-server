package kr.hhplus.be.server.reservation.infrastructure.persistence.seat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.model.Seat;
import kr.hhplus.be.server.reservation.domain.repository.SeatRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {

    private final SeatJpaRepository seatJpaRepository;
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository; // toEntity 변환 시 필요

    @Override
    public Seat save(Seat seat) {
        SeatEntity entity = toEntity(seat);
        SeatEntity savedEntity = seatJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Seat> findById(Long seatId) {
        return seatJpaRepository.findById(seatId).map(this::toDomain);
    }

    @Override
    public Optional<Seat> findByIdForUpdate(Long seatId) {
        return seatJpaRepository.findByIdForUpdate(seatId).map(this::toDomain);
    }

    @Override
    public List<Seat> findByConcertScheduleId(Long scheduleId) {
        return seatJpaRepository.findByConcertScheduleEntity_Id(scheduleId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // 도메인 모델 -> 영속성 엔티티 변환
    private SeatEntity toEntity(Seat seat) {
        return SeatEntity.builder()
                .id(seat.getId())
                .concertScheduleEntity(concertScheduleJpaRepository.getReferenceById(seat.getConcertScheduleId()))
                .seatNo(seat.getSeatNo())
                .price(seat.getPrice())
                .status(seat.getStatus()) // Seat 도메인에 status 필드가 있다고 가정
                .build();
    }

    // 영속성 엔티티 -> 도메인 모델 변환
    private Seat toDomain(SeatEntity entity) {
        return Seat.builder()
                .id(entity.getId())
                .concertScheduleId(entity.getConcertScheduleEntity().getId())
                .seatNo(entity.getSeatNo())
                .price(entity.getPrice())
                .status(entity.getStatus()) // SeatEntity에 status 필드가 있다고 가정
                .build();
    }
}