package com.ridelink.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.RegisterVehicleRequest;
import com.ridelink.driver.dto.VehicleData;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.security.JwtUtil;
import com.ridelink.driver.service.VehicleService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.ridelink.driver.config.SecurityConfig;
import com.ridelink.driver.security.JwtAuthenticationFilter;

@WebMvcTest(VehicleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
public class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "DRIVER", username = "11111111-1111-1111-1111-111111111111")
    public void testRegisterVehicle_Success() throws Exception {
        RegisterVehicleRequest request = new RegisterVehicleRequest();
        request.setMake("Honda");
        request.setModel("Civic");
        request.setYear(2022);
        request.setPlateNumber("XYZ-999");
        request.setCapacity(4);

        UUID driverId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        VehicleData data = new VehicleData();
        data.setDriverId(driverId);
        data.setPlateNumber("XYZ-999");
        data.setMake("Honda");
        data.setModel("Civic");
        data.setYear(2022);

        VehicleResponse response = new VehicleResponse(201, data);

        when(vehicleService.registerVehicle(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/vehicles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.plateNumber").value("XYZ-999"));
    }
}
