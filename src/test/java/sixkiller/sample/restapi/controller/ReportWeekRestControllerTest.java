package sixkiller.sample.restapi.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sixkiller.sample.repository.*;
import sixkiller.sample.restapi.dto.TimeEntryDto;
import sixkiller.sample.JoggingApplication;
import sixkiller.sample.domain.User;
import sixkiller.sample.restapi.ValidatingUserRepositoryDecorator;
import sixkiller.sample.restapi.dto.CreateUserDto;
import sixkiller.sample.security.RepositoryUserDetailsService;
import sixkiller.sample.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ala on 15.5.16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {JoggingApplication.class})
public class ReportWeekRestControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserService userService;

    @Autowired
    private TimeEntryRestController timeEntryRestController;

    @Autowired
    private ReportWeekRestController reportWeekRestController;

    @Autowired
    private RepositoryUserDetailsService repositoryUserDetailsService;

    private MockMvc mockMvc;

    private RestUtil restUtil;

    private User adminUser;

    @Before
    public void setUp() {
        UserRepository userRepository = new TestUserRepository();
        TimeEntryRepository timeEntryRepository = new TestTimeEntryRepository();
        ValidatingUserRepositoryDecorator validatingUserRepositoryDecorator =
                new ValidatingUserRepositoryDecorator(userRepository);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(springSecurityFilterChain).build();
        restUtil = new RestUtil(mockMvc);
        userService.setUserRepository(userRepository);
        repositoryUserDetailsService.setUserRepository(userRepository);
        timeEntryRestController.setTimeEntryRepository(timeEntryRepository);
        reportWeekRestController.setReportWeekRepository(new TestReportWeekRepository(timeEntryRepository));
        reportWeekRestController.setValidatingUserRepositoryDecorator(validatingUserRepositoryDecorator);
        timeEntryRestController.setValidatingUserRepositoryDecorator(validatingUserRepositoryDecorator);

        adminUser = restUtil.saveAdminUser(userRepository);
    }

    @Test
    public void unauthenticatedTest() throws Exception {
        String reportWeeksHref = restUtil.getReportWeeksHref(
                restUtil.userPost(restUtil.getCreateUser("user")).andReturn()
        );

        restUtil.performGet(reportWeeksHref, "").andExpect(status().isUnauthorized());
        restUtil.performGet(reportWeeksHref + "/20100101/20100101", "").andExpect(status().isUnauthorized());
    }

    @Test
    public void authenticatedTest() throws Exception {

        CreateUserDto user = restUtil.getCreateUser("user");

        MvcResult userMvcResult = restUtil.userPost(user).andExpect(status().isCreated())
                .andReturn();

        String userToken = restUtil.getAccessToken(user.getUserName(), user.getPassword());

        String timeEntriesHref = restUtil.getTimeEntriesHref(userMvcResult);
        String reportWeeksHref = restUtil.getReportWeeksHref(userMvcResult);

        for (int i = 0; i < 30; i++) {
            TimeEntryDto entry = restUtil.getTimeEntryDto(20160401 + i, 1, 1500);
            restUtil.timeEntryPost(entry, timeEntriesHref, userToken);
        }

        for (int i = 0; i < 5; i++) {
            TimeEntryDto entry = restUtil.getTimeEntryDto(20160501 + i, 1, 1500);
            restUtil.timeEntryPost(entry, timeEntriesHref, userToken);
        }

        restUtil.performGet(reportWeeksHref, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportWeeks", hasSize(6)));

        restUtil.performGet(reportWeeksHref + "/20160401/20160403", userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceSum", is(3.0)))
                .andExpect(jsonPath("$.timeSecondsSum", is(3 * 1500)));

        restUtil.performGet(reportWeeksHref + "/20160411/20160417", userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceSum", is(7.0)))
                .andExpect(jsonPath("$.timeSecondsSum", is(7 * 1500)));

        restUtil.performGet(reportWeeksHref + "/20160502/20160508", userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceSum", is(4.0)))
                .andExpect(jsonPath("$.timeSecondsSum", is(4 * 1500)));

    }


}
