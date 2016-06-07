package sixkiller.sample.restapi.exception;

/**
 * Created by ala on 12.5.16.
 */
public class DateParseException extends RuntimeException {

    private String field;
    private String value;

    public DateParseException(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

}
