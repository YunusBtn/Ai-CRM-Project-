package com.yunus.customer;

import com.yunus.common.dto.PageResponse;
import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.customer.dto.CustomerUpdateRequest;
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
    public CustomerResponse createCustomer(@RequestBody @Valid CustomerCreateRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID")
    public CustomerResponse getCustomerById(@PathVariable UUID id) {
        return customerService.getCustomerById(id);
    }

    @Operation(summary = "Get ALL Customers with pagination")
    @GetMapping("/all")
    public PageResponse<CustomerResponse> getAllCustomers(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return customerService.getAllCustomers(pageable);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update a customer by ID")
    public CustomerResponse updateCustomer(
            @PathVariable UUID id, @RequestBody @Valid CustomerUpdateRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
    }

    @PostMapping("/{customerId}/tags/{tagId}")
    @Operation(summary = "Add a tag to a customer")
    public CustomerResponse addTagToCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return customerService.addTagToCustomer(customerId, tagId);
    }


    @DeleteMapping("/{customerId}/tags/{tagId}")
    @Operation(summary = "Remove a tag from a customer")
    public CustomerResponse removeTagFromCustomer(
            @PathVariable UUID customerId,
            @PathVariable UUID tagId) {
        return customerService.removeTagFromCustomer(customerId, tagId);
    }


}
