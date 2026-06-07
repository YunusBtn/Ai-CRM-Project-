package com.yunus.dashboard;

import com.yunus.conversation.ConversationRepository;
import com.yunus.conversation.ConversationService;
import com.yunus.customer.CustomerRepository;
import com.yunus.customer.CustomerService;
import com.yunus.dashboard.dto.ConversationStatusCountResponse;
import com.yunus.dashboard.dto.DashboardSummaryResponse;
import com.yunus.dashboard.dto.TagCustomerCountResponse;
import com.yunus.enums.ConversationStatus;
import com.yunus.enums.CustomerStatus;
import com.yunus.enums.MessageDirection;
import com.yunus.message.MessageRepository;
import com.yunus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        List<ConversationStatus> activeStatuses = List.of(
                ConversationStatus.PENDING,
                ConversationStatus.OPEN
        );


        UUID currentUserId = getCurrentUserId();

        return new DashboardSummaryResponse(
                customerRepository.count(),
                customerRepository.countByStatus(CustomerStatus.ACTIVE),
                customerRepository.countByCreatedAtBetween(todayStart, tomorrowStart),
                conversationRepository.countByStatus(ConversationStatus.OPEN),
                conversationRepository.countByStatus(ConversationStatus.PENDING),
                conversationRepository.countWaitingConversations(activeStatuses, MessageDirection.INBOUND),
                messageRepository.countByDirectionAndSentAtBetween(MessageDirection.INBOUND, todayStart, tomorrowStart),
                conversationRepository.countByAssignedToIsNullAndStatusIn(activeStatuses),
                conversationRepository.countByStatusAndUpdatedAtBetween(ConversationStatus.CLOSED, todayStart, tomorrowStart),
                conversationRepository.countByAssignedToIdAndStatus(currentUserId, ConversationStatus.OPEN)
        );
    }

    @Transactional(readOnly = true)
    public List<ConversationStatusCountResponse> getConversationStatusDistribution() {
        return conversationRepository.countConversationsByStatus();
    }

    @Transactional(readOnly = true)
    public List<TagCustomerCountResponse> getCustomerTagDistribution() {
        return customerRepository.countCustomersByTag();
    }


    private UUID getCurrentUserId() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return currentUser.getUser().getId();
    }


}
