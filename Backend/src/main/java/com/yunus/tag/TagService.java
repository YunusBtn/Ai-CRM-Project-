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
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    private final TagMapper tagMapper;

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Etiket zaten mevcut");
        }

        Tag tag = tagMapper.toEntity(request);
        Tag savedTag = tagRepository.save(tag);
        
        log.info("Yeni etiket oluşturuldu: {}", savedTag.getName());

        return tagMapper.toResponse(savedTag);

    }

    @Transactional(readOnly = true)
    public TagResponse getTagById(UUID id) {

        Tag tag = findActiveTagById(id);
        return tagMapper.toResponse(tag);
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> getAllTags(Pageable pageable) {

        Page<TagResponse> tagPage = tagRepository.findAll(pageable)
                .map(tagMapper::toResponse);

        return PageResponse.from(tagPage);

    }

    @Transactional
    public TagResponse updateTag(UUID id, TagUpdateRequest request) {
        Tag tag = findActiveTagById(id);
        if (request.name()!= null && !request.name().isBlank()) {

            if (!tag.getName().equalsIgnoreCase(request.name())
                    && tagRepository.existsByNameIgnoreCase(request.name())) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Etiket zaten mevcut");
            }
            tag.setName(request.name());
        }

        if (request.color() != null) {
            tag.setColor(request.color());
        }
        Tag updatedTag = tagRepository.save(tag);
        log.info("Etiket güncellendi: {}", id);
        return tagMapper.toResponse(updatedTag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = findActiveTagById(id);
        tag.setDeleted(true); //soft delete
        tagRepository.save(tag);
        log.info("Etiket silindi: {}", id);
    }


    private Tag findActiveTagById(UUID id) {
        return tagRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND, "Etiket bulunamadı")
        );


    }

}
