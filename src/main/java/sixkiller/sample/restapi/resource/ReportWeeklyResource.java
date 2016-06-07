package sixkiller.sample.restapi.resource;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import sixkiller.sample.restapi.controller.ReportWeekRestController;
import sixkiller.sample.restapi.resource.report.ReportWeekResource;
import sixkiller.sample.domain.report.ReportWeek;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Created by ala on 12.5.16.
 */
public class ReportWeeklyResource extends ResourceSupport {

    private Collection<ReportWeekResource> reportWeeks;

    public ReportWeeklyResource(Collection<ReportWeek> reportWeeks, String userName) {

        this.reportWeeks = reportWeeks.stream()
                .map(
                        reportWeek -> new ReportWeekResource(reportWeek, userName)
                ).collect(Collectors.toList());

        add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(ReportWeekRestController.class, userName).getWeeklyReport(userName)
        ).withSelfRel());
    }

    public Collection<ReportWeekResource> getReportWeeks() {
        return reportWeeks;
    }
}
