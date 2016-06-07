package sixkiller.sample.repository;

import sixkiller.sample.domain.TimeEntry;
import sixkiller.sample.domain.report.ReportWeek;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ala on 15.5.16.
 */
public class TestReportWeekRepository extends ReportWeekRepository {

    private TimeEntryRepository timeEntryRepository;

    public TestReportWeekRepository(TimeEntryRepository timeEntryRepository) {
        super(null);
        this.timeEntryRepository = timeEntryRepository;
    }

    @Override
    public Collection<ReportWeek> getReportWeeks(String userName) {
        Collection<TimeEntry> timeEntries = timeEntryRepository.findByUserName(userName);

        return getReportWeeks(timeEntries, ViewType.FLAT);
    }

    @Override
    public Collection<ReportWeek> getReportWeeks(String userName, Integer dateFrom, Integer dateTo, ViewType viewType) {
        Collection<TimeEntry> timeEntries = timeEntryRepository.findByUserNameAndDateBetween(userName, dateFrom, dateTo);

        return getReportWeeks(timeEntries, viewType);
    }

    private Collection<ReportWeek> getReportWeeks(Collection<TimeEntry> timeEntries, ViewType viewType) {
        return timeEntries.stream()
                .collect(
                        Collectors.groupingBy(
                                TimeEntry::getWeek,
                                Collectors.toList()
                        )
                ).entrySet().stream()
                .map(
                        weekListEntry ->  {
                            ReportWeek reportWeek = new ReportWeek();
                            reportWeek.setWeek(weekListEntry.getKey());
                            if (ViewType.RICH.equals(viewType)) {
                                reportWeek.setTimeEntries(weekListEntry.getValue());
                            }
                            reportWeek.setDistanceSum(
                                    weekListEntry.getValue().stream().collect(
                                            Collectors.summingDouble(TimeEntry::getDistance))
                            );
                            reportWeek.setTimeSecondsSum(
                                    weekListEntry.getValue().stream().collect(
                                            Collectors.summingInt(TimeEntry::getTimeSeconds))
                            );

                            return reportWeek;
                        }
                ).collect(Collectors.toList());
    }
}
