package sixkiller.sample.domain;

/**
 * Created by ala on 12.5.16.
 */
public class Week {

    private Integer number;
    private Integer year;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Week withWeek(Integer week) {
        setNumber(week);

        return this;
    }

    public Week withYear(Integer year) {
        setYear(year);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Week week = (Week) o;

        if (!number.equals(week.number)) return false;
        return year.equals(week.year);

    }

    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }
}
