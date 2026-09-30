package com.ridelink.farepaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RideNotCompletedException extends RuntimeException {
    public RideNotCompletedException(String message) {
        super(message);
    }
}
