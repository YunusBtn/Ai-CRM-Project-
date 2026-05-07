package com.yunus.customer;

import com.yunus.common.PageResponse;
import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.customer.dto.CustomerUpdateRequest;
import com.yunus.tag.Tag;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final TagRepository tagRepository;

    //Customer Create
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {

        validatePhoneOrEmailPresent(request.phone(), request.email());
        validateEmailIsUnique(request.email());
        validatePhoneIsUnique(request.phone());

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    //Customer GetById
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = findActiveCustomerById(id);
        return customerMapper.toResponse(customer);
    }

    //Customer Delete
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = findActiveCustomerById(id);
        customer.setDeleted(true);
        customerRepository.save(customer);
    }

    //Customer getAll with pagination
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<CustomerResponse> customerPage = customerRepository.findAllByIsDeletedFalse(pageable)
                .map(customerMapper::toResponse);
        return PageResponse.from(customerPage);
    }

    //Customer Update
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {

        Customer customer = findActiveCustomerById(id);
        if (request.firstName() != null && !request.firstName().isBlank()) {
            customer.setFirstName(request.firstName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            customer.setLastName(request.lastName());
        }

        if (request.phone() != null) {
            String normalizedPhone = normalizeBlankToNull(request.phone());
            if (hasText(normalizedPhone)
                    && !request.phone().equals(customer.getPhone())
                    && customerRepository.existsByPhoneAndIsDeletedFalse(request.phone())) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Phone already exists");
            }
            customer.setPhone(normalizeBlankToNull(request.phone()));
        }


        if (request.email() != null) {
            String normalizedEmail = normalizeBlankToNull(request.email());

            if (hasText(normalizedEmail)
                    && !request.email().equalsIgnoreCase(customer.getEmail())
                    && customerRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.email())) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Email already exists");
            }
            customer.setEmail(normalizeBlankToNull(request.email()));
        }

        if (request.status() != null) {
            customer.setStatus(request.status());
        }

        validatePhoneOrEmailPresent(customer.getPhone(), customer.getEmail());

        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    //Tagg Add
    @Transactional
    public CustomerResponse addTagToCustomer(UUID customerId, UUID tagId) {

        Customer customer = findActiveCustomerById(customerId);
        Tag tag = findActiveTagById(tagId);

        customer.addTag(tag);

        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);


    }

    //Tagg Remove
    @Transactional
    public CustomerResponse removeTagFromCustomer(UUID customerId, UUID tagId) {

        Customer customer = findActiveCustomerById(customerId);
        Tag tag = findActiveTagById(tagId);

        customer.removeTag(tag);

        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(updatedCustomer);
    }


    private void validatePhoneOrEmailPresent(String phone, String email) {

        if (!hasText(phone) && !hasText(email)) {
            throw new BusinessException(ErrorType.VALIDATION_ERROR, "Phone or email must be provided");
        }

    }

    private void validateEmailIsUnique(String email) {

        if (!hasText(email)) {
            return;
        }
        if (customerRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Email already exists");
        }
    }


    private void validatePhoneIsUnique(String phone) {
        if (!hasText(phone)) {
            return;
        }
        if (customerRepository.existsByPhoneAndIsDeletedFalse(phone)) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Phone already exists");
        }
    }


    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeBlankToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Customer findActiveCustomerById(UUID id) {

        return customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Customer not found"));
    }

    private Tag findActiveTagById(UUID id) {
        return tagRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND, "Tag not found")
        );


    }
}
