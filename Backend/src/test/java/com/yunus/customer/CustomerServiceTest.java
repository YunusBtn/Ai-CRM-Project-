package com.yunus.customer;

import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerResponse;
import com.yunus.enums.CustomerStatus;
import com.yunus.exception.BusinessException;
import com.yunus.exception.ErrorType;
import com.yunus.tag.Tag;
import com.yunus.tag.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private CustomerService customerService;

    private UUID customerId;
    private UUID tagId;
    private Customer customer;
    private Tag tag;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        tagId = UUID.randomUUID();

        customer = new Customer();
        customer.setFirstName("Ali");
        customer.setLastName("Veli");
        customer.setEmail("ali@test.com");
        customer.setPhone("05001234567");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setDeleted(false);
        customer.setTags(new HashSet<>());

        tag = new Tag();
        tag.setName("vip");
        tag.setDeleted(false);

        customerResponse = new CustomerResponse(
                customerId, "Ali", "Veli",
                "05001234567", "ali@test.com",
                CustomerStatus.ACTIVE, Collections.emptySet(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createCustomer – validasyonlar
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createCustomer hem phone hem email null ise VALIDATION_ERROR fırlatılmalı")
    void createCustomer_WhenBothPhoneAndEmailNull_ShouldThrowValidationError() {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest("Ali", "Veli", null, null);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.VALIDATION_ERROR));

        verifyNoInteractions(customerRepository);
    }

    @Test
    @DisplayName("createCustomer hem phone hem email boş string ise VALIDATION_ERROR fırlatılmalı")
    void createCustomer_WhenBothPhoneAndEmailBlank_ShouldThrowValidationError() {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest("Ali", "Veli", "", "");

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.VALIDATION_ERROR));
    }

    @Test
    @DisplayName("createCustomer duplicate phone varsa DUPLICATE_ENTRY fırlatılmalı")
    void createCustomer_WhenDuplicatePhone_ShouldThrowDuplicateEntry() {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest("Ali", "Veli", "05001234567", null);
        when(customerRepository.existsByPhoneAndIsDeletedFalse("05001234567")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.DUPLICATE_ENTRY));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCustomer duplicate email varsa DUPLICATE_ENTRY fırlatılmalı")
    void createCustomer_WhenDuplicateEmail_ShouldThrowDuplicateEntry() {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest("Ali", "Veli", null, "ali@test.com");
        when(customerRepository.existsByEmailIgnoreCaseAndIsDeletedFalse("ali@test.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.DUPLICATE_ENTRY));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCustomer geçerli request ile customer kaydedilmeli ve response dönmeli")
    void createCustomer_WhenValid_ShouldSaveAndReturnResponse() {
        // Arrange
        CustomerCreateRequest request = new CustomerCreateRequest("Ali", "Veli", null, "ali@test.com");
        when(customerRepository.existsByEmailIgnoreCaseAndIsDeletedFalse("ali@test.com")).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        // Act
        CustomerResponse response = customerService.createCustomer(request);

        // Assert
        assertThat(response).isNotNull();
        verify(customerRepository).save(customer);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteCustomer – soft delete
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCustomer çağrıldığında isDeleted true yapılmalı ve save çağrılmalı")
    void deleteCustomer_WhenSuccess_ShouldMarkAsDeletedAndSave() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        assertThat(customer.isDeleted()).isTrue();
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("deleteCustomer customer bulunamazsa NOT_FOUND fırlatılmalı")
    void deleteCustomer_WhenCustomerNotFound_ShouldThrowNotFound() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(customerId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verify(customerRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // addTagToCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addTagToCustomer customer ve tag bulunmalı, tag customer.tags listesine eklenmeli")
    void addTagToCustomer_WhenSuccess_ShouldAddTagToCustomer() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(tagRepository.findByIdAndIsDeletedFalse(tagId)).thenReturn(Optional.of(tag));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        // Act
        customerService.addTagToCustomer(customerId, tagId);

        // Assert
        assertThat(customer.getTags()).contains(tag);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("addTagToCustomer customer bulunamazsa NOT_FOUND fırlatılmalı")
    void addTagToCustomer_WhenCustomerNotFound_ShouldThrowNotFound() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.addTagToCustomer(customerId, tagId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verifyNoInteractions(tagRepository);
    }

    @Test
    @DisplayName("addTagToCustomer tag bulunamazsa NOT_FOUND fırlatılmalı")
    void addTagToCustomer_WhenTagNotFound_ShouldThrowNotFound() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(tagRepository.findByIdAndIsDeletedFalse(tagId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.addTagToCustomer(customerId, tagId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verify(customerRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // removeTagFromCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeTagFromCustomer customer ve tag bulunmalı, tag customer.tags listesinden çıkarılmalı")
    void removeTagFromCustomer_WhenSuccess_ShouldRemoveTagFromCustomer() {
        // Arrange
        customer.getTags().add(tag); // önce tag'i ekle
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.of(customer));
        when(tagRepository.findByIdAndIsDeletedFalse(tagId)).thenReturn(Optional.of(tag));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        // Act
        customerService.removeTagFromCustomer(customerId, tagId);

        // Assert
        assertThat(customer.getTags()).doesNotContain(tag);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("removeTagFromCustomer customer bulunamazsa NOT_FOUND fırlatılmalı")
    void removeTagFromCustomer_WhenCustomerNotFound_ShouldThrowNotFound() {
        // Arrange
        when(customerRepository.findByIdAndIsDeletedFalse(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.removeTagFromCustomer(customerId, tagId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorType())
                        .isEqualTo(ErrorType.NOT_FOUND));

        verifyNoInteractions(tagRepository);
    }
}
