package kr.hhplus.be.server.queue.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.TokenStatus;
import kr.hhplus.be.server.queue.dto.request.QueueTokenIssueRequest;
import kr.hhplus.be.server.queue.dto.response.QueueStatusResponse;
import kr.hhplus.be.server.queue.dto.response.QueueTokenIssueResponse;
import kr.hhplus.be.server.queue.service.QueueTokenService;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QueueController.class)
public class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueueTokenService queueTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("대기열 토큰 발급 API가 정상 동작한다..")
    void issueToken_success() throws Exception {
        // given
        QueueTokenIssueRequest request = new QueueTokenIssueRequest(1L);

        User user = User.builder()
                .id(1L)
                .build();

        QueueToken token = QueueToken.builder()
                .user(user)
                .token("queue-token-123")
                .status(TokenStatus.WAITING)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        given(queueTokenService.issueToken(1L)).willReturn(token);

        // when & then
        mockMvc.perform(post("/api/queue/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.token").value("queue-token-123"))
            .andExpect(jsonPath("$.status").value(TokenStatus.WAITING.name()));

    }

    @Test
    @DisplayName("대기열 상태 조회 API가 성공적으로 응답을 반환한다.")
    void getStatus_success() throws Exception {
        // given
        String token = "queue-token-123";
        QueueStatusResponse response = new QueueStatusResponse(3, TokenStatus.WAITING.name());

        given(queueTokenService.checkStatus(token)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/queue/status")
                .param("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.position").value(3))
            .andExpect(jsonPath("$.status").value(TokenStatus.WAITING.name()));

    }


}
