package sixkiller.sample.repository.interceptor;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import sixkiller.sample.common.DateUtils;
import sixkiller.sample.domain.TimeEntry;

/**
 * Created by ala on 12.5.16.
 */
@Component
public class TimeEntryBeforeConvertListener extends AbstractMongoEventListener<TimeEntry> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<TimeEntry> event) {

        TimeEntry timeEntry = event.getSource();

        timeEntry.setWeek(
                DateUtils.getWeek(timeEntry.getDate())
        );

    }





}
