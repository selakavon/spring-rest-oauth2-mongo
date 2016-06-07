package sixkiller.sample.restapi.dto.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = GreaterThanValidator.class)
public @interface GreaterThan {

    String message() default "Not greater than minimum value";

    Class<?>[] groups() default { };

    double value() default 0;

    Class<? extends Payload>[] payload() default { };

}