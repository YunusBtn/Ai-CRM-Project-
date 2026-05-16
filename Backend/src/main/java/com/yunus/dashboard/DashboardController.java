package com.yunus.dashboard;

import com.yunus.dashboard.dto.ConversationStatusCountResponse;
import com.yunus.dashboard.dto.DashboardSummaryResponse;
import com.yunus.dashboard.dto.TagCustomerCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/conversation-status-distribution")
    public List<ConversationStatusCountResponse> getConversationStatusDistribution() {
        return dashboardService.getConversationStatusDistribution();
    }


    @GetMapping("/customer-tag-distribution")
    public List<TagCustomerCountResponse> getCustomerTagDistribution() {
        return dashboardService.getCustomerTagDistribution();
    }


}
