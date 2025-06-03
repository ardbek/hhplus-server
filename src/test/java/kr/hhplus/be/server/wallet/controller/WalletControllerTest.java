package kr.hhplus.be.server.wallet.controller;

import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.wallet.dto.request.BalanceChargeRequest;
import kr.hhplus.be.server.wallet.dto.request.WalletBalanceRequest;
import kr.hhplus.be.server.wallet.dto.response.BalanceChargeResponse;
import kr.hhplus.be.server.wallet.dto.response.WalletBalanceResponse;
import kr.hhplus.be.server.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("잔액 조회 API가 정상 동작한다.")
    void getBalance_success() throws Exception {
        // given
        WalletBalanceRequest request = new WalletBalanceRequest(1L);
        WalletBalanceResponse response = new WalletBalanceResponse(1L, 5_000L);

        given(walletService.getBalance(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/wallet")
            .param("userId", String.valueOf(request.userId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.balance").value(5_000L));

    }

    @Test
    @DisplayName("잔액 충전 API가 정상 동작한다.")
    void chargeBalance_success() throws Exception {
        Long walletId = 1L;
        Long chargeAmount = 2_000L;
        Long totalAmount = 7_000L;

        BalanceChargeRequest request = new BalanceChargeRequest(walletId, chargeAmount);
        BalanceChargeResponse response = new BalanceChargeResponse(walletId, totalAmount);

        given(walletService.charge(1L,chargeAmount)).willReturn(response);

        mockMvc.perform(post("/api/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId").value(1L))
            .andExpect(jsonPath("$.balance").value(totalAmount));

    }

}
