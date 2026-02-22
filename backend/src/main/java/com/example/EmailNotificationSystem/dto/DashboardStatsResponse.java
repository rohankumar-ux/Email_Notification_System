package com.example.EmailNotificationSystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalEmails;
    private long pending;
    private long queued;
    private long sent;
    private long delivered;
    private long failed;
    private long bounced;
}
