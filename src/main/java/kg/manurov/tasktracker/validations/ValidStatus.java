package kg.manurov.tasktracker.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = StatusValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatus {
    String message() default "Указан недопустимый статус задачи";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
