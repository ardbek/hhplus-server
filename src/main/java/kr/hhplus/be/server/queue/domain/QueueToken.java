package kr.hhplus.be.server.queue.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class QueueToken extends BaseTimeEntity {

    @Id
    private Long id;

    private Long userId;

    private String token;

    private Integer position;

    @Enumerated(EnumType.STRING)
    private TokenStatus status; // WAITING, ACTIVE, EXPIRED

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;

}
