package com.ridelink.driver.dto;

import java.util.List;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class VehicleListResponse {
    private String timestamp;
    private Integer status;
    private List<VehicleData> data;

    public VehicleListResponse(Integer status, List<VehicleData> data) {
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

    public List<VehicleData> getData() {
        return data;
    }

    public void setData(List<VehicleData> data) {
        this.data = data;
    }
}
