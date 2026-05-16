package com.yunus.conversation;

import com.yunus.auth.entity.User;
import com.yunus.conversation.dto.ConversationCreateRequest;
import com.yunus.conversation.dto.ConversationResponse;
import com.yunus.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {


    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Conversation toEntity(ConversationCreateRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "assignedToId", source = "assignedTo.id")
    @Mapping(target = "customerFullName", expression = "java(toCustomerFullName(conversation.getCustomer()))")
    @Mapping(target = "assignedToFullName", expression = "java(toUserFullName(conversation.getAssignedTo()))")
    ConversationResponse toResponse(Conversation conversation);


    default String toCustomerFullName(Customer customer) {
        if (customer == null) {
            return null;
        }
        return customer.getFirstName() + " " + customer.getLastName();
    }


    default String toUserFullName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }


}
