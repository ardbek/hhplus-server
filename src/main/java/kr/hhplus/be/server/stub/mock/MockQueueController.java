package kr.hhplus.be.server.stub.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.stub.mock.dto.request.MockTokenIssueRequest;
import kr.hhplus.be.server.stub.mock.dto.response.TokenIssueResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/queue-tokens")
@Tag(name = "Mock - 유저 대기열")
public class MockQueueController {

    @Operation(summary = "대기열 토큰 발급", description = "대기열 토큰과 순번을 응답")
    @PostMapping
    public ResponseEntity<TokenIssueResponse> issueToken(@RequestBody MockTokenIssueRequest request) {
        return ResponseEntity.ok(new TokenIssueResponse("mock-token-12345678", 1));
    }

}
