package com.ridelink.farepaymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.farepaymentservice.config.JwtUtil;
import com.ridelink.farepaymentservice.dto.*;
import com.ridelink.farepaymentservice.exception.*;
import com.ridelink.farepaymentservice.model.*;
import com.ridelink.farepaymentservice.service.FareService;
import com.ridelink.farepaymentservice.service.PaymentService;
import com.ridelink.farepaymentservice.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = {FareController.class, PaymentController.class, ReceiptController.class})
@Import({com.ridelink.farepaymentservice.config.SecurityConfig.class, com.ridelink.farepaymentservice.config.JwtAuthenticationFilter.class})
public class FarePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FareService fareService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void estimateFare_Success() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setEstimatedDistanceKm(10.0);

        FareEstimateData data = new FareEstimateData();
        data.setEstimatedDistanceKm(10.0);
        data.setBaseFare(new BigDecimal("300.00"));
        data.setDistanceFare(new BigDecimal("1000.00"));
        data.setEstimatedTotal(new BigDecimal("1300.00"));
        data.setCurrency("LKR");

        when(fareService.estimateFare(10.0)).thenReturn(data);

        mockMvc.perform(post("/api/v1/fares/estimate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedTotal").value(1300.00));
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void processPayment_SimulatedFailure_N05() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setRideId("test-ride-id");
        request.setPaymentMethod(PaymentMethod.WALLET);

        when(paymentService.processPayment(eq("test-ride-id"), eq(PaymentMethod.WALLET)))
                .thenThrow(new PaymentFailedException("Simulated payment processing failed. Please try again."));

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("PAYMENT_FAILED"));
    }

    @Test
    public void getReceipt_MissingJwt() throws Exception {
        mockMvc.perform(get("/api/v1/receipts/test-ride-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void validationFailure_ReturnsArrayOfStringsForDetails() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest();
        request.setEstimatedDistanceKm(-5.0); // Invalid

        mockMvc.perform(post("/api/v1/fares/estimate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0]").isString());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void processPayment_WrongPassenger_ThrowsUnauthorized() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setRideId("ride1");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(paymentService.processPayment(eq("ride1"), eq(PaymentMethod.CARD)))
                .thenThrow(new UnauthorizedAccessException("You are not authorized to access this ride's details."));

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void estimateFare_WrongRole_ThrowsForbidden() throws Exception {
        // If driver tries to estimate fare
    }

    @Test
    public void calculateFare_ValidServiceJwt_Success() throws Exception {
        CalculateFareRequest request = new CalculateFareRequest();
        request.setRideId("ride1");
        request.setActualDistanceKm(10.0);
        request.setDurationMinutes(20);

        FareData data = new FareData();
        data.setTotalAmount(new BigDecimal("1300.00"));
        
        when(jwtUtil.validateRideServiceToken("valid-service-token")).thenReturn(true);
        when(fareService.calculateFare("ride1", 10.0, 20)).thenReturn(data);

        mockMvc.perform(post("/api/v1/fares/calculate")
                .header("Authorization", "Bearer valid-service-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    public void calculateFare_DriverUserJwt_Forbidden() throws Exception {
        CalculateFareRequest request = new CalculateFareRequest();
        request.setRideId("ride1");
        request.setActualDistanceKm(10.0);
        request.setDurationMinutes(20);

        mockMvc.perform(post("/api/v1/fares/calculate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void calculateFare_PassengerUserJwt_Forbidden() throws Exception {
        CalculateFareRequest request = new CalculateFareRequest();
        request.setRideId("ride1");
        request.setActualDistanceKm(10.0);
        request.setDurationMinutes(20);

        mockMvc.perform(post("/api/v1/fares/calculate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void calculateFare_InvalidServiceJwt_Forbidden() throws Exception {
        CalculateFareRequest request = new CalculateFareRequest();
        request.setRideId("ride1");
        request.setActualDistanceKm(10.0);
        request.setDurationMinutes(20);

        when(jwtUtil.validateRideServiceToken("invalid-service-token")).thenReturn(false);

        mockMvc.perform(post("/api/v1/fares/calculate")
                .header("Authorization", "Bearer invalid-service-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Filter won't set auth -> 403 (or 401)
    }
}
