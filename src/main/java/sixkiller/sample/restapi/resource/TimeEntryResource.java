package sixkiller.sample.restapi.resource;

import org.springframework.hateoas.ResourceSupport;
import sixkiller.sample.restapi.controller.TimeEntryRestController;
import sixkiller.sample.domain.TimeEntry;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class TimeEntryResource extends ResourceSupport {

    private Integer date;

    private Double distance;

    private Integer timeSeconds;

    public TimeEntryResource(TimeEntry timeEntry) {

        this.date = timeEntry.getDate();
        this.distance = timeEntry.getDistance();
        this.timeSeconds = timeEntry.getTimeSeconds();

        this.add(linkTo(methodOn(
                TimeEntryRestController.class, timeEntry.getUserName()
                ).getTimeEntry(timeEntry.getUserName(), timeEntry.getId())
        ).withSelfRel());
    }

    public Integer getDate() {
        return date;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getTimeSeconds() {
        return timeSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeEntryResource that = (TimeEntryResource) o;

        if (!date.equals(that.date)) return false;
        if (!distance.equals(that.distance)) return false;
        return timeSeconds.equals(that.timeSeconds);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + distance.hashCode();
        result = 31 * result + timeSeconds.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TimeEntryResource{" +
                "date=" + date +
                ", distance=" + distance +
                ", timeSeconds=" + timeSeconds +
                '}';
    }
}
