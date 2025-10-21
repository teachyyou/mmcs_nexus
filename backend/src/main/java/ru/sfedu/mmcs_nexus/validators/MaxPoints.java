package ru.sfedu.mmcs_nexus.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;


@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotNull(message = "Поле не должно быть пустым")
@Min(value = 0)
@Max(value = 35)
public @interface MaxPoints {
    String message() default "Некорректное значение";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
