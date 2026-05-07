package com.yunus.tag;

import com.yunus.common.dto.PageResponse;
import com.yunus.tag.dto.TagCreateRequest;
import com.yunus.tag.dto.TagResponse;
import com.yunus.tag.dto.TagUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag management")
public class TagController {

    private final TagService tagService;


    @PostMapping("/create")
    @Operation(summary = "Create a new tag")
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.createTag(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tag by ID")
    public TagResponse getById(@PathVariable UUID id) {

        return tagService.getTagById(id);

    }

    @GetMapping("/all")
    @Operation(summary = "Get all tags")
    public PageResponse<TagResponse> getAllTags(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return tagService.getAllTags(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tag by ID")
    public TagResponse updateTag(@PathVariable UUID id, @RequestBody @Valid TagUpdateRequest request) {
        return tagService.updateTag(id, request);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
    }


}
