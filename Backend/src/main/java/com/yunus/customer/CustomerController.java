package com.yunus.customer;

import com.yunus.common.ApiResponse;
import com.yunus.common.PageResponse;
import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.customer.dto.CustomerUpdateRequest;
import com.yunus.enums.CustomerStatus;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new customer")
    public ApiResponse<CustomerResponse> createCustomer(@RequestBody @Valid CustomerCreateRequest request) {
        return ApiResponse.success(customerService.createCustomer(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        return ApiResponse.success(customerService.getCustomerById(id));
    }



    @Operation(summary = "Get ALL Customers with pagination and specification")
    @GetMapping("/all")
    public ApiResponse<PageResponse<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false)  UUID tagId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(customerService.getAllCustomers(search,status,tagId,pageable));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update a customer by ID")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable UUID id, @RequestBody @Valid CustomerUpdateRequest request) {
        return ApiResponse.success(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ApiResponse.success(null, "Müşteri başarıyla silindi");
    }

    @PostMapping("/{customerId}/tags/{tagId}")
    @Operation(summary = "Add a tag to a customer")
    public ApiResponse<CustomerResponse> addTagToCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return ApiResponse.success(customerService.addTagToCustomer(customerId, tagId));
    }


    @DeleteMapping("/{customerId}/tags/{tagId}")
    @Operation(summary = "Remove a tag from a customer")
    public ApiResponse<CustomerResponse> removeTagFromCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return ApiResponse.success(customerService.removeTagFromCustomer(customerId, tagId));
    }


}
