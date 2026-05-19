package com.yunus.ai;

import com.yunus.ai.dto.AiResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AiResultMapper {

    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "requestedById", source = "requestedBy.id")
    @Mapping(target = "requestedByFullName", expression = "java(getRequestedByFullName(aiResult))")
    AiResultResponse toResponse(AiResult aiResult);

    default String getRequestedByFullName(AiResult aiResult) {
        if (aiResult == null) {
            return null;
        }

        if (aiResult.getRequestedBy() == null) {
            return null;
        }

        String firstName = aiResult.getRequestedBy().getFirstName();
        String lastName = aiResult.getRequestedBy().getLastName();

        String safeFirstName = firstName != null ? firstName : "";
        String safeLastName = lastName != null ? lastName : "";

        String fullName = (safeFirstName + " " + safeLastName).trim();

        return fullName.isEmpty() ? null : fullName;
    }
}