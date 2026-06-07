package com.yunus.tag;

import com.yunus.tag.dto.TagCreateRequest;
import com.yunus.tag.dto.TagResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

    Tag toEntity(TagCreateRequest request);

    TagResponse toResponse(Tag tag);

}
