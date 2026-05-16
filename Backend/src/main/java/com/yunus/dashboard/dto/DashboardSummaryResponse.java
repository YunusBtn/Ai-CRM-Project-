package com.yunus.dashboard.dto;

public record DashboardSummaryResponse(

        long totalCustomerCount,
        long activeCustomerCount,
        long todayCreatedCustomerCount,
        long openConversationCount,
        long pendingConversationCount,
        long waitingReplyConversationCount,
        long todayInboundMessageCount,
        long unassignedConversationCount,
        long todayClosedConversationCount,
        long myAssignedOpenConversationCount

) {
}
