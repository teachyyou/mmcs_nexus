package ru.sfedu.mmcs_nexus.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "Email не должен быть пустым")
@Email(message = "Некорректный email")
@Pattern(
        regexp = "^[^@\\s]+@sfedu\\.ru$",
        message = "Email должен оканчиваться на @sfedu.ru"
)
public @interface UserEmail {
    String message() default "Email должен оканчиваться на @sfedu.ru";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}