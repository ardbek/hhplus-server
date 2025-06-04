package kr.hhplus.be.server.reservation.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpa;

    public ReservationRepositoryImpl(ReservationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = toEntity(reservation);
        ReservationEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsLocked(Long seatId, Long concertScheduleId) {
        return jpa.findBySeatIdAndConcertScheduleIdAndStatus(seatId, concertScheduleId, ReservationStatus.LOCKED)
                .isPresent();
    }

    // 매핑 함수
    private ReservationEntity toEntity(Reservation r) {
        ReservationEntity e = new ReservationEntity();
        e.id = r.getId();
        e.userId = r.getUserId();
        e.concertScheduleId = r.getConcertScheduleId();
        e.seatId = r.getSeatId();
        e.status = r.getStatus();
        e.createdAt = r.getCreatedAt();
        e.updatedAt = r.getUpdatedAt();
        return e;
    }

    private Reservation toDomain(ReservationEntity e) {
        return Reservation.builder()
                .id(e.id)
                .userId(e.userId)
                .concertScheduleId(e.concertScheduleId)
                .seatId(e.seatId)
                .status(e.status)
                .createdAt(e.createdAt)
                .updatedAt(e.updatedAt)
                .build();
    }

}
