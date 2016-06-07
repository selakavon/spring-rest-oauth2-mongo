package sixkiller.sample.restapi.resource.report;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import sixkiller.sample.common.DateUtils;
import sixkiller.sample.configuration.WeekFieldsFactory;
import sixkiller.sample.restapi.controller.ReportWeekRestController;
import sixkiller.sample.domain.report.ReportWeek;
import sixkiller.sample.restapi.resource.TimeEntryResource;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by ala on 12.5.16.
 */
public class ReportWeekResource extends ResourceSupport {

    public static final WeekFields WEEK_FIELDS = WeekFieldsFactory.weekFields();

    private int weekNumber;

    private int weekYear;

    private Double distanceSum;

    private Integer timeSecondsSum;

    private Integer weekFirstDate;

    private Integer weekLastDate;

    private Collection<TimeEntryResource> timeEntries;

    public ReportWeekResource(ReportWeek reportWeek, String userName) {

        this.weekNumber = reportWeek.getWeek().getNumber();
        this.weekYear = reportWeek.getWeek().getYear();

        this.distanceSum = reportWeek.getDistanceSum();
        this.timeSecondsSum = reportWeek.getTimeSecondsSum();

        LocalDate weekFirstLocalDate = LocalDate.now().withYear(weekYear).with(
                WEEK_FIELDS.weekOfYear(), weekNumber
        ).with(
                WEEK_FIELDS.dayOfWeek(), WEEK_FIELDS.getFirstDayOfWeek().getValue()
        );

        LocalDate weekLastLocalDate = weekFirstLocalDate.plusDays(6);

        this.weekFirstDate = DateUtils.intDate(weekFirstLocalDate);
        this.weekLastDate = DateUtils.intDate(weekLastLocalDate);

        if (reportWeek.getTimeEntries() != null) {
            this.timeEntries = reportWeek.getTimeEntries().stream()
                    .map(timeEntry -> new TimeEntryResource(timeEntry))
                    .collect(Collectors.toList());
        }

        add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(
                ReportWeekRestController.class, userName)
                .getWeek(userName, weekFirstDate, weekLastDate)
        ).withSelfRel());
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public int getWeekYear() {
        return weekYear;
    }

    public Double getDistanceSum() {
        return distanceSum;
    }

    public Integer getTimeSecondsSum() {
        return timeSecondsSum;
    }

    public Integer getWeekFirstDate() {
        return weekFirstDate;
    }

    public Integer getWeekLastDate() {
        return weekLastDate;
    }

    public Collection<TimeEntryResource> getTimeEntries() {
        return timeEntries;
    }
}
