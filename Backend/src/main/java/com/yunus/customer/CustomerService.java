package com.yunus.customer;

import com.yunus.common.PageResponse;
import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.customer.dto.CustomerUpdateRequest;
import com.yunus.enums.CustomerStatus;
import com.yunus.tag.Tag;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final TagRepository tagRepository;

    //Customer Create
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        validateEmailIsUnique(request.email());
        validatePhoneIsUnique(request.phone());

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Yeni müşteri oluşturuldu: {} {}", savedCustomer.getFirstName(), savedCustomer.getLastName());

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
        log.info("Müşteri silindi: {}", id);
    }

    //Customer getAll with pagination
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> getAllCustomers(
            String search,
            CustomerStatus status,
            UUID tagId,
            Pageable pageable) {

        Specification<Customer> specification = Specification
                .where(CustomerSpecification.conjunction())
                .and(CustomerSpecification.containsSearch(search))
                .and(CustomerSpecification.hasStatus(status))
                .and(CustomerSpecification.hasTag(tagId));


        Page<CustomerResponse> customerPage = customerRepository.findAll(specification,pageable)
                .map(customerMapper::toResponse);
        return PageResponse.from(customerPage);
    }

    //Customer Update
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {

        Customer customer = findActiveCustomerById(id);
        if (StringUtils.hasText(request.firstName())) {
            customer.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            customer.setLastName(request.lastName());
        }

        if (request.phone() != null) {
            String normalizedPhone = request.phone().trim();
            if (StringUtils.hasText(normalizedPhone)
                    && !normalizedPhone.equals(customer.getPhone())
                    && customerRepository.existsByPhone(normalizedPhone)) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Telefon numarası zaten kayıtlı");
            }
            customer.setPhone(StringUtils.hasText(normalizedPhone) ? normalizedPhone : null);
        }

        if (request.email() != null) {
            String normalizedEmail = request.email().trim();

            if (StringUtils.hasText(normalizedEmail)
                    && !normalizedEmail.equalsIgnoreCase(customer.getEmail())
                    && customerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "E-posta adresi zaten kayıtlı");
            }
            customer.setEmail(StringUtils.hasText(normalizedEmail) ? normalizedEmail : null);
        }

        if (request.status() != null) {
            customer.setStatus(request.status());
        }

        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Müşteri güncellendi: {}", id);

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


    private void validateEmailIsUnique(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "E-posta adresi zaten kayıtlı");
        }
    }


    private void validatePhoneIsUnique(String phone) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        if (customerRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorType.DUPLICATE_ENTRY, "Telefon numarası zaten kayıtlı");
        }
    }

    private Customer findActiveCustomerById(UUID id) {

        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorType.NOT_FOUND, "Customer not found"));
    }

    private Tag findActiveTagById(UUID id) {
        return tagRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorType.NOT_FOUND, "Tag not found")
        );


    }
}
