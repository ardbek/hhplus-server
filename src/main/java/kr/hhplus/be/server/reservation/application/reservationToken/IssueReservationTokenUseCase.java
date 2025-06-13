package kr.hhplus.be.server.reservation.application.reservationToken;

import java.util.UUID;
import kr.hhplus.be.server.queue.exception.AlreadyInQueueException;
import kr.hhplus.be.server.reservation.domain.repository.ReservationTokenRepository;
import kr.hhplus.be.server.user.exception.UserNotFoundException;
import kr.hhplus.be.server.user.repository.UserRepository;

public class IssueReservationTokenUseCase {

    private final UserRepository userRepository;
    private final ReservationTokenRepository tokenRepository;

    public IssueReservationTokenUseCase(UserRepository userRepository,
            ReservationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public String issueReservationToken(Long userId) {

        // 존재하는 사용자인지 확인
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 이미 활성 상태인지 확인
        if (tokenRepository.isActiveUser(userId)) {
            throw new AlreadyInQueueException(); // todo 예외 분리
        }

        // 이미 대기열에 있는지 확인
        if (tokenRepository.getRank(userId) != null) {
            throw new AlreadyInQueueException();
        }

        // 토큰 생성 및 저장
        String token = UUID.randomUUID().toString();
        tokenRepository.saveToken(token, userId);

        // 대기열에 추가
        tokenRepository.addWaiting(userId);

        return token;
    }
}
