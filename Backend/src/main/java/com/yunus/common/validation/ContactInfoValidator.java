package com.yunus.common.validation;

import com.yunus.customer.dto.CustomerCreateRequest;
import com.yunus.customer.dto.CustomerUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class ContactInfoValidator implements ConstraintValidator<ValidContactInfo, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof CustomerCreateRequest request) {
            return StringUtils.hasText(request.phone()) || StringUtils.hasText(request.email());
        }
        if (value instanceof CustomerUpdateRequest request) {
            return StringUtils.hasText(request.phone()) || StringUtils.hasText(request.email());
        }
        return true;
    }
}
