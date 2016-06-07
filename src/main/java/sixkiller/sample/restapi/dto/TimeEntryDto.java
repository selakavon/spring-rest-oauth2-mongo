package sixkiller.sample.restapi.dto;

import sixkiller.sample.restapi.dto.validator.GreaterThan;
import sixkiller.sample.restapi.dto.validator.IntDateValidate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by ala on 9.5.16.
 */
public class TimeEntryDto {

    public static final int DATE_MIN = 19700101;
    public static final int DATE_MAX = 21001231;

    @NotNull
    @Min(DATE_MIN)
    @Max(DATE_MAX)
    @IntDateValidate
    private Integer date;

    @NotNull
    @GreaterThan(0)
    private Double distance;

    @NotNull
    @Min(1)
    private Integer timeSeconds;

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(Integer timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public TimeEntryDto withDate(Integer date) {
        setDate(date);
        return this;
    }

    public TimeEntryDto withDistance(Double distance) {
        setDistance(distance);
        return this;
    }

    public TimeEntryDto withTimeSeconds(Integer timeSeconds) {
        setTimeSeconds(timeSeconds);
        return this;
    }

}
