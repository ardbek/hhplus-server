package kr.hhplus.be.server.reservation.interfaces.web.controller;

import kr.hhplus.be.server.queue.dto.request.QueueTokenIssueRequest;
import kr.hhplus.be.server.reservation.application.reservationToken.CheckQueueStatusUseCase;
import kr.hhplus.be.server.reservation.application.reservationToken.IssueReservationTokenUseCase;
import kr.hhplus.be.server.reservation.interfaces.web.dto.response.reservation.ReservationTokenStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class ReservationTokenController {

    private final IssueReservationTokenUseCase issueReservationTokenUseCase;
    private final CheckQueueStatusUseCase checkQueueStatusUseCase;

    // 대기열 토큰 발급 api
    @PostMapping("/tokens")
    public ResponseEntity<String> issue(@RequestBody QueueTokenIssueRequest request) {
        String token = issueReservationTokenUseCase.issueReservationToken(request.userId());

        /*QueueTokenIssueResponse queueTokenIssueResponse = new QueueTokenIssueResponse(
                token.getUser().getId(), token.getToken(), token.getStatus().name(), token.getIssuedAt(),
                token.getExpiresAt());*/

        return ResponseEntity.ok(token);
    }

    // 대기 번호 조회 api
    @GetMapping("/status")
    public ResponseEntity<ReservationTokenStatusResponse> getStatus(@RequestParam String token) {

        ReservationTokenStatusResponse response = checkQueueStatusUseCase.checkStatus(token);
        return ResponseEntity.ok(response);
    }

}
