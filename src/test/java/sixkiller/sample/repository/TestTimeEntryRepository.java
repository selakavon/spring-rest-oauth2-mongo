package sixkiller.sample.repository;

import sixkiller.sample.common.DateUtils;
import sixkiller.sample.domain.TimeEntry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ala on 9.5.16.
 */
public class TestTimeEntryRepository extends DummyAbstractRepository<TimeEntry, String>
        implements TimeEntryRepository {

    Collection<TimeEntry> timeEntryCollection = new ArrayList<>();

    @Override
    public Collection<TimeEntry> findByUserName(String userName) {
        return new ArrayList<>(timeEntryCollection).stream().filter(timeEntry -> userName.equals(timeEntry.getUserName()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<TimeEntry> findByUserNameAndDateBetween(String userName, Integer dateFrom, Integer dateTo) {
        return new ArrayList<>(timeEntryCollection).stream()
                .filter(timeEntry ->
                        timeEntry.getDate() >= dateFrom && timeEntry.getDate() <= dateTo
                ).collect(Collectors.toList());
    }

    @Override
    public void deleteByUserName(String userName) {
        Objects.requireNonNull(userName);

        timeEntryCollection.removeIf(timeEntry -> userName.equals(timeEntry.getUserName()));
    }

    @Override
    public List<TimeEntry> findAll() {
        return new ArrayList<>(timeEntryCollection);
    }

    @Override
    public TimeEntry findOne(String id) {
        Objects.requireNonNull(id);

        return new ArrayList<>(timeEntryCollection).stream()
                .filter(timeEntry -> id.equals(timeEntry.getId()))
                .findAny()
                .orElse(null);
    }

    @Override
    public TimeEntry save(TimeEntry entity) {
        if (entity.getId() == null) {
            entity.setId(IDGenerator.getNextId());
        } else {
            Optional<TimeEntry> timeEntryOptional = timeEntryCollection.stream()
                    .filter(timeEntry -> entity.getId().equals(timeEntry.getId()))
                    .findAny();

            if (timeEntryOptional.isPresent()) {
                timeEntryCollection.remove(timeEntryOptional.get());
            }
        }

        entity.setWeek(
                DateUtils.getWeek(entity.getDate())
        );

        timeEntryCollection.add(entity);

        return entity;
    }

    @Override
    public void delete(String id) {
        Objects.requireNonNull(id);

        timeEntryCollection.removeIf(timeEntry -> id.equals(timeEntry.getId()));
    }

    @Override
    public void delete(TimeEntry entity) {
        Objects.requireNonNull(entity);

        delete(entity.getId());
    }
}
