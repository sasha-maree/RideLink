package com.ridelink.rideservice.controller;

import com.ridelink.rideservice.config.JwtUtil;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.UpdateRideStatusRequest;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatusAction;
import com.ridelink.rideservice.service.RideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRequestRide() throws Exception {
        UUID accountId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                accountId, "dummyJwt", java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        CreateRideRequest request = new CreateRideRequest();
        request.setPickupLocation("Location A");
        request.setDropoffLocation("Location B");

        Ride ride = new Ride();
        when(rideService.requestRide(any(), any())).thenReturn(ride);

        mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdateRideStatus() throws Exception {
        UUID accountId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                accountId, "dummyJwt", java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_DRIVER")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateRideStatusRequest request = new UpdateRideStatusRequest();
        request.setAction(RideStatusAction.ACCEPT);

        Ride ride = new Ride();
        when(rideService.updateRideStatus(any(), any(), any())).thenReturn(ride);

        mockMvc.perform(patch("/api/v1/rides/" + UUID.randomUUID() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRideById_ServiceJwt() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "FARE_PAYMENT_SERVICE", "dummyJwt", java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SERVICE")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        Ride ride = new Ride();
        when(rideService.getRideById(any(), any(), any())).thenReturn(ride);

        mockMvc.perform(get("/api/v1/rides/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
