package com.example.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UrlFormatValidator.class)  // points Bean Validation at the logic below
public @interface ValidUrl {
    String message() default "must be a valid http or https URL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
