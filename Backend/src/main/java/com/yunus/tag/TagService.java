package com.yunus.tag;

import com.yunus.common.PageResponse;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.tag.dto.TagCreateRequest;
import com.yunus.tag.dto.TagResponse;
import com.yunus.tag.dto.TagUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    private final TagMapper tagMapper;

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (tagRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Tag already exists");
        }

        Tag tag = tagMapper.toEntity(request);
        Tag savedTag = tagRepository.save(tag);

        return tagMapper.toResponse(savedTag);

    }

    @Transactional(readOnly = true)
    public TagResponse getTagById(UUID id) {

        Tag tag = findActiveTagById(id);
        return tagMapper.toResponse(tag);
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> getAllTags(Pageable pageable) {

        Page<TagResponse> tagPage = tagRepository.findAllByIsDeletedFalse(pageable)
                .map(tagMapper::toResponse);

        return PageResponse.from(tagPage);

    }

    @Transactional
    public TagResponse updateTag(UUID id, TagUpdateRequest request) {
        Tag tag = findActiveTagById(id);
        if (request.name()!= null && !request.name().isBlank()) {

            if (!tag.getName().equalsIgnoreCase(request.name())
                    && tagRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Tag already exists");
            }
            tag.setName(request.name());
        }

        if (request.color() != null) {
            tag.setColor(request.color());
        }
        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = findActiveTagById(id);
        tag.setDeleted(true); //soft delete
        tagRepository.save(tag);
    }


    private Tag findActiveTagById(UUID id) {
        return tagRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND, "Tag not found")
        );


    }

}
