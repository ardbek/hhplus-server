package kr.hhplus.be.server.reservation.infrastructure.persistence.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import kr.hhplus.be.server.reservation.domain.ReservationStatus;
import kr.hhplus.be.server.reservationInfo.domain.ConcertSchedule;
import kr.hhplus.be.server.reservationInfo.domain.Seat;
import kr.hhplus.be.server.user.domain.User;
import lombok.Getter;

@Getter
@Entity
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_schedule_id")
    ConcertSchedule concertSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    Seat seat;

    @Enumerated(EnumType.STRING)
    ReservationStatus status; // LOCKED, CONFIRMED, RELEASED

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    protected ReservationEntity() {}


}
