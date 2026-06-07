package com.yunus.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactInfoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidContactInfo {
    String message() default "Telefon veya e-posta adresinden en az biri sağlanmalıdır";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
