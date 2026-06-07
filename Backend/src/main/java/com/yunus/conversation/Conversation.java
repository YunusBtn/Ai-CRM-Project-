package com.yunus.conversation;

import com.yunus.auth.entity.User;
import com.yunus.common.BaseEntity;
import com.yunus.customer.Customer;
import com.yunus.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Conversation extends BaseEntity {


    private String title;
    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    private ConversationStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    public void changeStatus(ConversationStatus status) {
        this.status = status;
    }

    public void assignTo(User user) {
        this.assignedTo = user;
    }


}
