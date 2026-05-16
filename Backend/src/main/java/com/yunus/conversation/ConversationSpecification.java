package com.yunus.conversation;

import com.yunus.enums.ConversationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ConversationSpecification {

    private ConversationSpecification() {
    }


    public static Specification<Conversation> isDeletedFalse() {
        return (root, query, cb) ->
                cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Conversation> hasStatus(ConversationStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Conversation> assignedTo(UUID assignedToId) {
        return (root, query, cb) -> {
            if (assignedToId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("assignedTo").get("id"), assignedToId);
        };
    }

    public static Specification<Conversation> belongsToCustomer(UUID customerId) {
        return (root, query, cb) -> {
            if (customerId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("customer").get("id"), customerId);
        };
    }


    public static Specification<Conversation> isUnassigned(Boolean unassigned) {
        return (root, query, cb) -> {
            if (unassigned == null || !unassigned) {
                return cb.conjunction();
            }
            return cb.isNull(root.get("assignedTo"));
        };
    }


}
