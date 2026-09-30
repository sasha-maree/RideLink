package com.ridelink.farepaymentservice.controller;

import com.ridelink.farepaymentservice.dto.ReceiptResponse;
import com.ridelink.farepaymentservice.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<ReceiptResponse> getReceiptByRideId(@PathVariable String rideId, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return ResponseEntity.ok(new ReceiptResponse(200, receiptService.getReceipt(rideId, token)));
    }
}
