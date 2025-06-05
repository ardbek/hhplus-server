package kr.hhplus.be.server.queue.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import kr.hhplus.be.server.user.domain.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenStatus status; // WAITING, ACTIVE, EXPIRED

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;

    public void active() {
        this.status = TokenStatus.ACTIVE;
    }

}
