package com.yunus.customer.dto;

import com.yunus.common.validation.ValidContactInfo;
import com.yunus.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@ValidContactInfo
public record CustomerUpdateRequest(


        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,

        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,

        @Size(min = 11, message = "Phone number must be at least 11 characters long")
        String phone,

        @Size(min = 5, message = "Email must be at least 5 characters long")
        @Email(message = "Email format is invalid")
        String email,

        CustomerStatus status

) {
}
