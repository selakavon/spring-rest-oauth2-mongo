package sixkiller.sample.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import sixkiller.sample.domain.report.ReportWeek;

import java.util.Collection;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by ala on 12.5.16.
 */

@Service
public class ReportWeekRepository {

    public static final Logger LOGGER = LoggerFactory.getLogger(ReportWeekRepository.class);

    private MongoTemplate mongoTemplate;

    @Autowired
    public ReportWeekRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Collection<ReportWeek> getReportWeeks(String userName) {
        return getReportWeeks(userName, null, null, ViewType.FLAT);
    }

    public Collection<ReportWeek> getReportWeeks(String userName, Integer dateFrom, Integer dateTo, ViewType viewType) {

        Criteria criteria = getCriteria(userName, dateFrom, dateTo);

        GroupOperation groupOperation = getGroupOperation(viewType);

        ProjectionOperation projection = getProjection(viewType);

        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                groupOperation,
                projection

        );

        AggregationResults<ReportWeek> results = mongoTemplate.aggregate(aggregation, "timeEntry", ReportWeek.class);

        logDebug(aggregation, results);

        return results.getMappedResults();
    }

    private ProjectionOperation getProjection(ViewType viewType) {
        ProjectionOperation projection = project("timeSecondsSum", "distanceSum").and("_id").as("week");

        if (ViewType.RICH.equals(viewType)) {
            projection = projection.and("timeEntries").as("timeEntries");
        }

        return projection;
    }

    private GroupOperation getGroupOperation(ViewType viewType) {
        GroupOperation groupOperation = group("week.number", "week.year")
                .sum("distance").as("distanceSum")
                .sum("timeSeconds").as("timeSecondsSum");

        if (ViewType.RICH.equals(viewType)) {
            groupOperation = groupOperation.push("$$CURRENT").as("timeEntries");
        }

        return groupOperation;
    }

    private void logDebug(Aggregation aggregation, AggregationResults<ReportWeek> results) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Query used: {} \n" +
                            "Found: {} \n" +
                            "Mapped: {}",
                    aggregation,
                    results.getRawResults(),
                    results.getMappedResults()
            );
        }
    }

    private Criteria getCriteria(String userName, Integer dateFrom, Integer dateTo) {
        Criteria criteria = where("userName").is(userName);

        if (dateFrom != null || dateTo != null) {
            criteria = criteria.and("date");
        }

        if (dateFrom != null) {
            criteria = criteria.gte(dateFrom);
        }

        if (dateTo != null) {
            criteria = criteria.lte(dateTo);
        }
        return criteria;
    }

    public static enum ViewType {
        FLAT,
        RICH;
    }
}
