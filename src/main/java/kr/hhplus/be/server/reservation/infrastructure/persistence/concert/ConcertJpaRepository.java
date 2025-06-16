package kr.hhplus.be.server.reservation.infrastructure.persistence.concert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<ConcertEntity, Long> {
} 