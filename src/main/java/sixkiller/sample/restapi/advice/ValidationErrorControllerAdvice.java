package sixkiller.sample.restapi.advice;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sixkiller.sample.restapi.exception.DateParseException;

@ControllerAdvice("sixkiller.sample.restapi.controller")
public class ValidationErrorControllerAdvice {

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors userNotFoundExceptionHandler(MethodArgumentNotValidException ex) {
        return new VndErrors("validation_error", errorMessage(ex.getBindingResult().getFieldError()));
    }

    @ResponseBody
    @ExceptionHandler(DateParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors dateParseExceptionHandler(DateParseException ex) {
        return new VndErrors("validation_error", errorMessage(ex.getField(), ex.getValue()));
    }

    private String errorMessage(FieldError fieldError) {
        return errorMessage(fieldError.getField(), fieldError.getRejectedValue());
    }

    private String errorMessage(String field, Object value) {

        return String.format("Field [%s] validation failed : rejected value [%s]",
                field, value);

    }

}
