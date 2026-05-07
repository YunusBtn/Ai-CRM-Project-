package com.yunus.note;

import com.yunus.auth.entity.User;
import com.yunus.note.dto.NoteCreateRequest;
import com.yunus.note.dto.NoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "conversation", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Note toEntity(NoteCreateRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByFullName", expression = "java(toUserFullName(note.getCreatedBy()))")
    NoteResponse toResponse(Note note);

    default String toUserFullName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }


}
