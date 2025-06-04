package kr.hhplus.be.server.reservation.domain;

import java.util.List;

public enum ReservationStatus {
    LOCKED, CONFIRMED, RELEASED;

    public static List<ReservationStatus> reservedStatuses() {
        return List.of(LOCKED, CONFIRMED);
    }
}
