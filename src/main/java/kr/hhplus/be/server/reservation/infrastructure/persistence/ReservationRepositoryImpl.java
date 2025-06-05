package kr.hhplus.be.server.reservation.infrastructure.persistence;

import java.util.Optional;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservation.domain.model.Reservation;
import kr.hhplus.be.server.reservation.domain.repository.ReservationRepository;
import kr.hhplus.be.server.reservationInfo.repository.ConcertScheduleRepository;
import kr.hhplus.be.server.reservationInfo.repository.SeatRepository;
import kr.hhplus.be.server.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository jpa;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ConcertScheduleRepository concertScheduleRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository jpa, UserRepository userRepository,
            SeatRepository seatRepository, ConcertScheduleRepository concertScheduleRepository) {
        this.jpa = jpa;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.concertScheduleRepository = concertScheduleRepository;
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
        return jpa.findBySeatIdAndConcertScheduleIdAndStatus(seatId, concertScheduleId, ReservationStatus.LOCKED).isPresent();
    }

    private ReservationEntity toEntity(Reservation r) {
        ReservationEntity e = new ReservationEntity();
        e.id = r.getId();
        e.user = userRepository.getReferenceById(r.getUserId());
        e.concertSchedule = concertScheduleRepository.getReferenceById(r.getConcertScheduleId());
        e.seat = seatRepository.getReferenceById(r.getSeatId());
        e.status = r.getStatus();
        e.createdAt = r.getCreatedAt();
        e.updatedAt = r.getUpdatedAt();
        return e;
    }

    private Reservation toDomain(ReservationEntity e) {
        return Reservation.builder()
                .id(e.id)
                .userId(e.user.getId())
                .concertScheduleId(e.concertSchedule.getId())
                .seatId(e.seat.getId())
                .status(e.status)
                .createdAt(e.createdAt)
                .updatedAt(e.updatedAt)
                .build();
    }

}
