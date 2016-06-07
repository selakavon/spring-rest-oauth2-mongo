package sixkiller.sample.restapi.dto.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by ala on 15.5.16.
 */
public class GreaterThanValidator implements ConstraintValidator<GreaterThan, Double> {

    GreaterThan constraintAnnotation;

    @Override
    public void initialize(GreaterThan constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        double min = constraintAnnotation.value();

        return value > min;
    }
}
