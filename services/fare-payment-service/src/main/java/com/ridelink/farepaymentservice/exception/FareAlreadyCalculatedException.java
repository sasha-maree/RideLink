package com.ridelink.farepaymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FareAlreadyCalculatedException extends RuntimeException {
    public FareAlreadyCalculatedException(String message) {
        super(message);
    }
}
