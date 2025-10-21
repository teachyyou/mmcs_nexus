package ru.sfedu.mmcs_nexus.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "Поле не должно быть пустым")
@Size(max = 20, message = "Не должно превышать 20 символов")
@Pattern(
        regexp = "^(?=.{1,20}$)[А-ЯЁа-яё]+(?:-[А-ЯЁа-яё]+)*$",
        message = "Допустимы только русские буквы и дефис между частями"
)
public @interface UserName {
    String message() default "Некорректное значение";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}