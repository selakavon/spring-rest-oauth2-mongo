package sixkiller.sample.domain.report;

import sixkiller.sample.domain.Week;
import sixkiller.sample.domain.TimeEntry;

import java.util.Collection;

/**
 * Created by ala on 12.5.16.
 */
public class ReportWeek {

    private Week week;

    private Double distanceSum;

    private int timeSecondsSum;

    private Collection<TimeEntry> timeEntries;

    public Week getWeek() {
        return week;
    }

    public void setWeek(Week week) {
        this.week = week;
    }

    public Double getDistanceSum() {
        return distanceSum;
    }

    public void setDistanceSum(Double distanceSum) {
        this.distanceSum = distanceSum;
    }

    public int getTimeSecondsSum() {
        return timeSecondsSum;
    }

    public void setTimeSecondsSum(int timeSecondsSum) {
        this.timeSecondsSum = timeSecondsSum;
    }

    public Collection<TimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(Collection<TimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }
}
