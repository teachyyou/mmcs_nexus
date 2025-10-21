package ru.sfedu.mmcs_nexus.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Min(value = 2020)
@Max(value= 2030)
public @interface EventYear {
    String message() default "Некорректное значение";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
