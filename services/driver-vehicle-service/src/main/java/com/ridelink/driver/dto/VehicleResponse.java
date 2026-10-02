package com.ridelink.driver.dto;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class VehicleResponse {
    private String timestamp;
    private Integer status;
    private VehicleData data;

    public VehicleResponse(Integer status, VehicleData data) {
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        this.status = status;
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public VehicleData getData() {
        return data;
    }

    public void setData(VehicleData data) {
        this.data = data;
    }
}
