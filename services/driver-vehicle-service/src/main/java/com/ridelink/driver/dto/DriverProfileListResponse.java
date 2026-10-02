package com.ridelink.driver.dto;

import java.util.List;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DriverProfileListResponse {
    private String timestamp;
    private Integer status;
    private DriverProfileListData data;

    public DriverProfileListResponse(Integer status, DriverProfileListData data) {
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

    public DriverProfileListData getData() {
        return data;
    }

    public void setData(DriverProfileListData data) {
        this.data = data;
    }

    public static class DriverProfileListData {
        private List<DriverProfileData> items;
        private Long total;
        private Integer page;
        private Integer size;

        public DriverProfileListData(List<DriverProfileData> items, Long total, Integer page, Integer size) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.size = size;
        }

        public List<DriverProfileData> getItems() {
            return items;
        }

        public void setItems(List<DriverProfileData> items) {
            this.items = items;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
