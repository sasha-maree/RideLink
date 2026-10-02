package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.CreateDriverProfileRequest;
import com.ridelink.driver.dto.DriverProfileData;
import com.ridelink.driver.dto.DriverProfileResponse;
import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.security.JwtUtil;
import com.ridelink.driver.service.DriverProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.ridelink.driver.config.SecurityConfig;
import com.ridelink.driver.security.JwtAuthenticationFilter;

@WebMvcTest(DriverProfileController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class DriverProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverProfileService driverProfileService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DRIVER", username = "11111111-1111-1111-1111-111111111111")
    public void testCreateDriverProfile_Success() throws Exception {
        CreateDriverProfileRequest request = new CreateDriverProfileRequest();
        request.setLicenseNumber("LIC-1234");

        UUID driverId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        DriverProfileData data = new DriverProfileData();
        data.setDriverId(driverId);
        data.setLicenseNumber("LIC-1234");
        data.setAvailabilityStatus(AvailabilityStatus.OFFLINE);

        DriverProfileResponse response = new DriverProfileResponse(201, data);

        when(driverProfileService.createProfile(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/drivers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.driverId").value(driverId.toString()))
                .andExpect(jsonPath("$.data.licenseNumber").value("LIC-1234"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "22222222-2222-2222-2222-222222222222")
    public void testListAllDrivers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(status().isOk());
    }
}
