package kr.hhplus.be.server.reservation.domain.repository;

import java.util.Set;

public interface ReservationTokenRepository {

    /**
     * 대기열 토큰 저장
     * @param token - 저장할 토큰(key)
     * @param userId - 유저 번호 (value)
     */
    void saveToken(String token, Long userId);

    /**
     * 토큰으로 유저 Id 조회
     * @param token - 토큰
     * @return
     */
    Long getUserIdByToken(String token);

    /**
     * 대기열에 유저 추가
     * @param userId
     */
    void addWaiting(Long userId);

    /**
     * 대기열에서 유저 대기순번 조회
     * @param userId
     * @return
     */
    Long getRank(Long userId);

    /**
     * 활성화 될 최상위 유저 조회
     * @param count
     * @return
     */
    Set<Long> getTopRankedUsers(long count);

    /**
     * 대기열에서 유저 제거
     * @param userId
     */
    void removeFromWaiting(Long userId);

    /**
     * 유저를 활성 상태로 변경
     * @param userId
     */
    void setActiveUser(Long userId);

    /**
     * 유저가 활성 상태인지 확인
     * @param userId
     * @return
     */
    boolean isActiveUser(Long userId);

}
