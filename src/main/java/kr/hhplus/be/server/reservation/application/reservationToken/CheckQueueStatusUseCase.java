package kr.hhplus.be.server.reservation.application.reservationToken;

import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.exception.TokenNotFoundException;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.ReservationTokenStatusResponse;

public class CheckQueueStatusUseCase {

    private final ReservationTokenRepository tokenRepository;
    private static final int ACTIVE_USER_COUNT = 50; // 활성화 시킬 사용자 수

    public CheckQueueStatusUseCase(ReservationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * 대기 순서 확인
     * @param token
     * @return
     */
    public ReservationTokenStatusResponse checkStatus(String token) {
        // 대기열에 있는 사용자인지 확인
        Long userId = tokenRepository.getUserIdByToken(token);
        if(userId == null) {
            throw new TokenNotFoundException();
        }

        // 이미 활성 상태인지 확인
        if (tokenRepository.isActiveUser(userId)) {
            return new ReservationTokenStatusResponse(TokenStatus.ACTIVE.name(), 0L);
        }

        // 대기 순번 확인
        Long rank = tokenRepository.getRank(userId);
        if(rank == null) {
            throw new TokenNotFoundException();
        }

        // 아직 대기중인 경우
        return new ReservationTokenStatusResponse(TokenStatus.WAITING.name(), rank + 1);

    }


}
