package kr.hhplus.be.server.queue.controller;

import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.dto.request.QueueTokenIssueRequest;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;
import kr.hhplus.be.server.queue.service.QueueTokenService;
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
public class QueueController {

    private final QueueTokenService queueTokenService;

    // 대기열 토큰 발급 api
    @PostMapping("/token")
    public ResponseEntity<QueueTokenIssueResponse> issue(@RequestBody QueueTokenIssueRequest request) {
        QueueToken token = queueTokenService.issueToken(request.userId());

        QueueTokenIssueResponse queueTokenIssueResponse = new QueueTokenIssueResponse(
                token.getUser().getId(), token.getToken(), token.getStatus().name(), token.getIssuedAt(),
                token.getExpiresAt());

        return ResponseEntity.ok(queueTokenIssueResponse);
    }

    // 대기 번호 조회 api
    @GetMapping("/status")
    public ResponseEntity<QueueStatusResponse> getStatus(@RequestParam String token) {
        return ResponseEntity.ok(queueTokenService.checkStatus(token));
    }

}
