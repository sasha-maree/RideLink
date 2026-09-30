package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountData;
import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.model.Role;
import com.ridelink.account.security.JwtAuthenticationFilter;
import com.ridelink.account.security.JwtUtil;
import com.ridelink.account.service.AccountManagementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountManagementService accountManagementService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetAccountById_Success() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountData data = new AccountData();
        data.setAccountId(accountId);
        data.setRole(Role.PASSENGER);
        AccountResponse response = new AccountResponse(200, data);

        Mockito.when(accountManagementService.getAccountById(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountId").value(accountId.toString()));
    }
}
