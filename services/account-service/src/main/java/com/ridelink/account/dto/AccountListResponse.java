package com.ridelink.account.dto;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class AccountListResponse {
    private String timestamp;
    private int status;
    private AccountListData data;

    public AccountListResponse() {}

    public AccountListResponse(int status, AccountListData data) {
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public AccountListData getData() {
        return data;
    }

    public void setData(AccountListData data) {
        this.data = data;
    }

    public static class AccountListData {
        private List<AccountData> items;
        private long total;
        private int page;
        private int size;

        public AccountListData() {}

        public AccountListData(List<AccountData> items, long total, int page, int size) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.size = size;
        }

        public List<AccountData> getItems() {
            return items;
        }

        public void setItems(List<AccountData> items) {
            this.items = items;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
