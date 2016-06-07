package sixkiller.sample.restapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sixkiller.sample.domain.TimeEntry;
import sixkiller.sample.repository.TimeEntryRepository;
import sixkiller.sample.restapi.ValidatingUserRepositoryDecorator;
import sixkiller.sample.restapi.dto.TimeEntryDto;
import sixkiller.sample.restapi.resource.ResourceCollection;
import sixkiller.sample.restapi.resource.TimeEntryResource;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users/{userName}/time-entries")
public class TimeEntryRestController {

    public static final String OWNER = "authentication.name == #userName";
    public static final String ADMIN = "hasRole('ADMIN')";

    private ValidatingUserRepositoryDecorator validatingUserRepositoryDecorator;

    private TimeEntryRepository timeEntryRepository;

    @PreAuthorize(ADMIN + " or " + OWNER)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ResourceCollection<TimeEntryResource>> getTimeEntries(@PathVariable String userName,
                                                                                @RequestParam(required = false) Integer dateFrom,
                                                                                @RequestParam(required = false) Integer dateTo)  {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        Integer dateFromNotNull = dateFrom == null ? TimeEntryDto.DATE_MIN : dateFrom;
        Integer dateToNotNull = dateTo == null ? TimeEntryDto.DATE_MAX : dateTo;

        Collection<TimeEntryResource> timeEntryResourceCollection = timeEntryRepository
                .findByUserNameAndDateBetween(userName, dateFromNotNull - 1, dateToNotNull + 1)
                .stream().map((timeEntry -> new TimeEntryResource(timeEntry)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ResourceCollection<>(timeEntryResourceCollection)
        );
    }


    @RequestMapping(method = RequestMethod.GET, path = "/{timeEntryId}")
    @PreAuthorize(ADMIN + " or " + OWNER)
    public ResponseEntity<TimeEntryResource> getTimeEntry(@PathVariable String userName,
                                                          @PathVariable String timeEntryId) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        TimeEntry timeEntry = timeEntryRepository.findOne(timeEntryId);

        if (timeEntry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(
                new TimeEntryResource(timeEntry)
        );
    }

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize(ADMIN + " or " + OWNER)
    public ResponseEntity<TimeEntryResource> createTimeEntry(@PathVariable String userName,
                                                             @Validated @RequestBody TimeEntryDto timeEntryDto) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setUserName(userName);

        updateTimeEntry(timeEntry, timeEntryDto);

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);

        return ResponseEntity.created(
                linkTo(methodOn(TimeEntryRestController.class).getTimeEntry(userName, savedTimeEntry.getId()))
                        .toUri()
        ).body(
                new TimeEntryResource(savedTimeEntry)
        );
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{timeEntryId}")
    @PreAuthorize(ADMIN + " or " + OWNER)
    public ResponseEntity<TimeEntryResource> modifyTimeEntry(@PathVariable String userName,
                                                             @PathVariable String timeEntryId,
                                                             @Validated @RequestBody TimeEntryDto timeEntryDto) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        TimeEntry timeEntry = timeEntryRepository.findOne(timeEntryId);

        if (timeEntry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        timeEntry.setUserName(userName);
        updateTimeEntry(timeEntry, timeEntryDto);

        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);

        return ResponseEntity.ok(
                new TimeEntryResource(savedTimeEntry)
        );
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{timeEntryId}")
    @PreAuthorize(ADMIN + " or " + OWNER)
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable String userName,
                                                @PathVariable String timeEntryId) {

        validatingUserRepositoryDecorator.findAccountValidated(userName);

        timeEntryRepository.delete(timeEntryId);

        return ResponseEntity.ok().build();
    }

    private void updateTimeEntry(TimeEntry timeEntry, TimeEntryDto timeEntryDto) {

        timeEntry.setDate(timeEntryDto.getDate());
        timeEntry.setDistance(timeEntryDto.getDistance());
        timeEntry.setTimeSeconds(timeEntryDto.getTimeSeconds());

    }

    @Autowired
    public void setValidatingUserRepositoryDecorator(ValidatingUserRepositoryDecorator validatingUserRepositoryDecorator) {
        this.validatingUserRepositoryDecorator = validatingUserRepositoryDecorator;
    }

    @Autowired
    public void setTimeEntryRepository(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }
}
