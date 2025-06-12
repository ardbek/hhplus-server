package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.concertSchedule.ConcertScheduleJpaRepository;
import kr.hhplus.be.server.reservation.infrastructure.persistence.seat.SeatJpaRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpa;
    private final UserRepository userRepository;
    private final SeatJpaRepository seatJpaRepository;
    private final ConcertScheduleJpaRepository concertScheduleJpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository jpa, UserRepository userRepository,
            SeatJpaRepository seatJpaRepository, ConcertScheduleJpaRepository concertScheduleJpaRepository) {
        this.jpa = jpa;
        this.userRepository = userRepository;
        this.seatJpaRepository = seatJpaRepository;
        this.concertScheduleJpaRepository = concertScheduleJpaRepository;
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
        return jpa.findReservation(seatId, concertScheduleId, ReservationStatus.LOCKED).isPresent();
    }

    @Override
    public List<Long> findByReservedSeatIds(Long scheduleId, List<ReservationStatus> statuses) {
        return jpa.findByReservedSeatIds(scheduleId, statuses);
    }

    // 만료된 모든 예약 조회
    @Override
    public List<Reservation> findReservationsToExpire(ReservationStatus status, LocalDateTime now) {
        return jpa.findReservationsToExpire(status, now)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ReservationEntity toEntity(Reservation r) {
        ReservationEntity e = new ReservationEntity();
        e.id = r.getId();
        e.user = userRepository.getReferenceById(r.getUserId());
        e.concertScheduleEntity = concertScheduleJpaRepository.getReferenceById(r.getConcertScheduleId());
        e.seatEntity = seatJpaRepository.getReferenceById(r.getSeatId());
        e.status = r.getStatus();
        e.createdAt = r.getCreatedAt();
        e.updatedAt = r.getUpdatedAt();
        return e;
    }

    private Reservation toDomain(ReservationEntity e) {
        return Reservation.builder()
                .id(e.id)
                .userId(e.user.getId())
                .concertScheduleId(e.concertScheduleEntity.getId())
                .seatId(e.seatEntity.getId())
                .status(e.status)
                .createdAt(e.createdAt)
                .updatedAt(e.updatedAt)
                .build();
    }

}
