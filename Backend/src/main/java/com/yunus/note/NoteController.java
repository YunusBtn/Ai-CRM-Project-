package com.yunus.note;

import com.yunus.common.ApiResponse;
import com.yunus.common.PageResponse;
import com.yunus.note.dto.NoteCreateRequest;
import com.yunus.note.dto.NoteResponse;
import com.yunus.note.dto.NoteUpdateRequest;
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
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Note management endpoints")
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "Create note for customer")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("create/{customerId}")
    public ApiResponse<NoteResponse> createCustomerNote(
            @PathVariable UUID customerId,
            @Valid @RequestBody NoteCreateRequest request
    ) {
        return ApiResponse.success(noteService.createCustomerNote(customerId, request));
    }

    @Operation(summary = "Get notes by customer id")
    @GetMapping("{customerId}")
    public ApiResponse<PageResponse<NoteResponse>> getByCustomerId(
            @PathVariable UUID customerId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.success(noteService.getByCustomerId(customerId, pageable));
    }

    @Operation(summary = "Create note for conversation")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("conversationCreate/{conversationId}")
    public ApiResponse<NoteResponse> createConversationNote(
            @PathVariable UUID conversationId,
            @Valid @RequestBody NoteCreateRequest request
    ) {
        return ApiResponse.success(noteService.createConversationNote(conversationId, request));
    }

    @Operation(summary = "Get notes by conversation id")
    @GetMapping("getConversation/{conversationId}")
    public ApiResponse<PageResponse<NoteResponse>> getByConversationId(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.success(noteService.getByConversationId(conversationId, pageable));
    }

    @Operation(summary = "Get note by id")
    @GetMapping("get/{id}")
    public ApiResponse<NoteResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(noteService.getById(id));
    }

    @Operation(summary = "Update note")
    @PutMapping("update/{id}")
    public ApiResponse<NoteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody NoteUpdateRequest request
    ) {
        return ApiResponse.success(noteService.update(id, request));
    }

    @Operation(summary = "Delete note by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("delete/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        noteService.delete(id);
        return ApiResponse.success(null, "Not başarıyla silindi");
    }
}
