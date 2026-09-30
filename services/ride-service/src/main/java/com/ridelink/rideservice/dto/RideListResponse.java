package com.ridelink.rideservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RideListResponse {
    private LocalDateTime timestamp;
    private int status;
    private RideListData data;

    public RideListResponse() {}

    public RideListResponse(LocalDateTime timestamp, int status, List<RideData> items, int total, int page, int size) {
        this.timestamp = timestamp;
        this.status = status;
        this.data = new RideListData(items, total, page, size);
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public RideListData getData() { return data; }
    public void setData(RideListData data) { this.data = data; }

    public static class RideListData {
        private List<RideData> items;
        private int total;
        private int page;
        private int size;

        public RideListData() {}

        public RideListData(List<RideData> items, int total, int page, int size) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.size = size;
        }

        public List<RideData> getItems() { return items; }
        public void setItems(List<RideData> items) { this.items = items; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
