package sixkiller.sample.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import sixkiller.sample.domain.TimeEntry;

import java.util.Collection;

public interface TimeEntryRepository extends MongoRepository<TimeEntry, String> {

    Collection<TimeEntry> findByUserName(String userName);

    Collection<TimeEntry> findByUserNameAndDateBetween(
            String userName, Integer dateFrom, Integer dateTo);

    void deleteByUserName(String userName);
}
