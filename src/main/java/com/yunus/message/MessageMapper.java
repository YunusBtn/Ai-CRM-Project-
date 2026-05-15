package com.yunus.message;

import com.yunus.auth.entity.User;
import com.yunus.message.dto.MessageCreateRequest;
import com.yunus.message.dto.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "senderUser", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "direction", source = "messageDirection")
    Message toEntity(MessageCreateRequest request);

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderUserId", source = "senderUser.id")
    @Mapping(target = "senderUserFullName", expression = "java(toUserFullName(message.getSenderUser()))")
    MessageResponse toResponse(Message message);

    default String toUserFullName(User user){
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }


}
