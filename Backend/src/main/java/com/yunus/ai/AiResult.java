package com.yunus.ai;

import com.yunus.enums.AiResultStatus;
import com.yunus.enums.AiResultType;
import com.yunus.auth.entity.User;
import com.yunus.common.BaseEntity;
import com.yunus.conversation.Conversation;
import com.yunus.customer.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ai_results")
public class AiResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AiResultType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AiResultStatus status;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 50)
    private String promptVersion;

    @Column(columnDefinition = "TEXT")
    private String inputSnapshot;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private Integer inputTokenEstimate;

    @Column
    private Integer outputTokenEstimate;

    @Column
    private Integer maxOutputTokens;
}