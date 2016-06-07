package sixkiller.sample.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sixkiller.sample.domain.report.ReportWeek;
import sixkiller.sample.repository.ReportWeekRepository;
import sixkiller.sample.restapi.ValidatingUserRepositoryDecorator;
import sixkiller.sample.restapi.resource.ReportWeeklyResource;
import sixkiller.sample.restapi.resource.report.ReportWeekResource;

import java.util.Collection;

/**
 * Created by ala on 12.5.16.
 */

@RestController
@RequestMapping("/api/users/{userName}/report-weeks")
public class ReportWeekRestController {

    public static final String OWNER = "authentication.name == #userName";
    public static final String ADMIN = "hasRole('ADMIN')";

    private ValidatingUserRepositoryDecorator validatingUserRepositoryDecorator;

    private ReportWeekRepository reportWeekRepository;


    @PreAuthorize(ADMIN + " or " + OWNER)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ReportWeeklyResource> getWeeklyReport(@PathVariable String userName) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        return ResponseEntity.ok(
                new ReportWeeklyResource(reportWeekRepository.getReportWeeks(userName), userName)
        );

    }

    @PreAuthorize(ADMIN + " or " + OWNER)
    @RequestMapping(path = "/{dateFrom}/{dateTo}", method = RequestMethod.GET)
    public ResponseEntity<ReportWeekResource> getWeek(@PathVariable String userName,
                                                      @PathVariable Integer dateFrom,
                                                      @PathVariable Integer dateTo) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        Collection<ReportWeek> reportWeeks = reportWeekRepository.getReportWeeks(
                userName, dateFrom, dateTo, ReportWeekRepository.ViewType.RICH);

        if (reportWeeks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (reportWeeks.size() > 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return ResponseEntity.ok(
                new ReportWeekResource(reportWeeks.iterator().next(), userName)
        );

    }

    @Autowired
    public void setValidatingUserRepositoryDecorator(ValidatingUserRepositoryDecorator validatingUserRepositoryDecorator) {
        this.validatingUserRepositoryDecorator = validatingUserRepositoryDecorator;
    }

    @Autowired
    public void setReportWeekRepository(ReportWeekRepository reportWeekRepository) {
        this.reportWeekRepository = reportWeekRepository;
    }
}
