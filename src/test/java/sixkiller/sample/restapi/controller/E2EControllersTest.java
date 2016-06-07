package sixkiller.sample.restapi.controller;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
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
import sixkiller.sample.JoggingApplication;
import sixkiller.sample.domain.User;
import sixkiller.sample.restapi.ValidatingUserRepositoryDecorator;
import sixkiller.sample.restapi.dto.CreateUserDto;
import sixkiller.sample.restapi.dto.ModifyUserDto;
import sixkiller.sample.restapi.dto.TimeEntryDto;
import sixkiller.sample.security.RepositoryUserDetailsService;
import sixkiller.sample.service.UserService;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ala on 15.5.16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {JoggingApplication.class})
public class E2EControllersTest {

    public static final String ROOT_HREF = "/api/";
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
        userService.setTimeEntryRepository(timeEntryRepository);
        repositoryUserDetailsService.setUserRepository(userRepository);
        timeEntryRestController.setTimeEntryRepository(timeEntryRepository);
        reportWeekRestController.setReportWeekRepository(new TestReportWeekRepository(timeEntryRepository));
        reportWeekRestController.setValidatingUserRepositoryDecorator(validatingUserRepositoryDecorator);
        timeEntryRestController.setValidatingUserRepositoryDecorator(validatingUserRepositoryDecorator);

        adminUser = restUtil.saveAdminUser(userRepository);
    }

    @Test
    public void userFLowTest() throws Exception {
        /*
        Register new user.
         */
        CreateUserDto user = restUtil.getCreateUser("user");
        restUtil.userPost(user)
                .andExpect(status().isCreated())
                .andExpect(header().string("location", notNullValue()));
        /*
        Login
         */
        String userToken = restUtil.getAccessToken(user.getUserName(), user.getPassword());

        /*
        Fetch Users resource href
         */
        MvcResult rootMvcResult = restUtil.performGet(ROOT_HREF, "").andExpect(status().isOk())
                .andReturn();
        String usersHref = restUtil.getUsersHref(rootMvcResult);

        /*
        Fetch logged user info
         */
        MvcResult userMvcResult = restUtil.performGet(usersHref + "/" + user.getUserName(), userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(user.getUserName())))
                .andExpect(jsonPath("$.fullName", is(user.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", contains(RestUtil.USER_ROLE)))
                .andReturn();

        /*
        Store user location (self href) and time entries and report weeks hrefs.
         */
        String timeEntriesHref = restUtil.getTimeEntriesHref(userMvcResult);
        String reportWeeksHref = restUtil.getReportWeeksHref(userMvcResult);

        /*
        Fetch Time Entries
         */
        restUtil.performGet(timeEntriesHref, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        /*
        Add a time entry
         */
        TimeEntryDto entry = restUtil.getTimeEntryDto(20160510, 12.5, 3600);
        MvcResult entryMvcResult = restUtil.timeEntryPost(entry, timeEntriesHref, userToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date", is(entry.getDate())))
                .andExpect(jsonPath("$.distance", is(entry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(entry.getTimeSeconds())))
                .andExpect(header().string("location", notNullValue()))
                .andReturn();

        /*
        Read entry by href in location header
         */
        String entryLocation = restUtil.getLocation(entryMvcResult);
        restUtil.performGet(entryLocation, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is(entry.getDate())))
                .andExpect(jsonPath("$.distance", is(entry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(entry.getTimeSeconds())));

        /*
        Modify entry
         */
        entry = restUtil.getTimeEntryDto(20160509, 13, 3300);
        restUtil.timeEntryPut(entry, entryLocation, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is(entry.getDate())))
                .andExpect(jsonPath("$.distance", is(entry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(entry.getTimeSeconds())));

        /*
        Check entry's new values
         */
        restUtil.performGet(entryLocation, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is(entry.getDate())))
                .andExpect(jsonPath("$.distance", is(entry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(entry.getTimeSeconds())));

        /*
        Delete entry
         */
        restUtil.timeEntryDelete(entryLocation, userToken)
                .andExpect(status().isOk());

        /*
        Check entry does not exist anymore
         */
        restUtil.performGet(entryLocation, userToken)
                .andExpect(status().isNotFound());

        restUtil.performGet(timeEntriesHref, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        /*
        Add some time entries
         */
        TimeEntryDto entry1Week1 = restUtil.getTimeEntryDto(20160509, 13, 3300);
        restUtil.timeEntryPost(entry1Week1, timeEntriesHref, userToken)
                .andExpect(status().isCreated());

        TimeEntryDto entry2Week1 = restUtil.getTimeEntryDto(20160510, 15, 4000);
        restUtil.timeEntryPost(entry2Week1, timeEntriesHref, userToken)
                .andExpect(status().isCreated());

        TimeEntryDto entry1Week2 = restUtil.getTimeEntryDto(20160518, 11, 2023);
        restUtil.timeEntryPost(entry1Week2, timeEntriesHref, userToken)
                .andExpect(status().isCreated());

        /*
        Fetch Report Weeks (FLAT view without time entries)
        */
        MvcResult reportWeeksMvcResult = restUtil.performGet(reportWeeksHref, userToken)
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.reportWeeks", hasSize(2)))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==20)].distanceSum", contains(
                        entry1Week2.getDistance()
                )))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==20)].timeSecondsSum", contains(
                        entry1Week2.getTimeSeconds()
                )))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==20)].weekFirstDate", contains(20160516)))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==20)].weekLastDate", contains(20160522)))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==20)].timeEntries", contains((Object)null)))

                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==19)].distanceSum", contains(
                        entry1Week1.getDistance() + entry2Week1.getDistance()
                )))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==19)].timeSecondsSum", contains(
                        entry1Week1.getTimeSeconds() + entry2Week1.getTimeSeconds()
                )))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==19)].weekFirstDate", contains(20160509)))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==19)].weekLastDate", contains(20160515)))
                .andExpect(jsonPath("$.reportWeeks[?(@.weekNumber==19)].timeEntries", contains((Object)null)))
                .andReturn();

        /*
        Fetch one week with time entries (RICH view)
         */
        net.minidev.json.JSONArray jsonArray = JsonPath.read(reportWeeksMvcResult.getResponse().getContentAsString(),
                "$.reportWeeks[?(@.weekNumber==20)]._links.self.href");;
        String reportWeekHref = jsonArray.get(0).toString();

        restUtil.performGet(reportWeekHref, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceSum", is(
                        entry1Week2.getDistance()
                )))
                .andExpect(jsonPath("$.timeSecondsSum", is(
                        entry1Week2.getTimeSeconds()
                )))
                .andExpect(jsonPath("$.weekFirstDate", is(20160516)))
                .andExpect(jsonPath("$.weekLastDate", is(20160522)))
                .andExpect(jsonPath("$.timeEntries", hasSize(1)));
    }

    @Test
    public void adminFlowTest() throws Exception {
       /*
        Register new users.
         */
        CreateUserDto vincent = restUtil.getCreateUser("vincent");
        restUtil.userPost(vincent)
                .andExpect(status().isCreated())
                .andExpect(header().string("location", notNullValue()));

        CreateUserDto jules = restUtil.getCreateUser("jules");
        restUtil.userPost(jules)
                .andExpect(status().isCreated())
                .andExpect(header().string("location", notNullValue()));
        /*
        Login users
         */
        restUtil.getAccessToken(vincent.getUserName(), vincent.getPassword());
        restUtil.getAccessToken(jules.getUserName(), jules.getPassword());

        /*
        Fetch Users resource href
         */
        MvcResult rootMvcResult = restUtil.performGet(ROOT_HREF, "").andExpect(status().isOk())
                .andReturn();
        String usersHref = restUtil.getUsersHref(rootMvcResult);

        /*
        Login as admin
         */
        String adminToken = restUtil.getAccessToken(adminUser.getUserName(), adminUser.getPassword());

        /*
        Get users list
         */
        MvcResult usersMvcResult = restUtil.performGet(usersHref, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[?(@.userName=='vincent')]", hasSize(1)))
                .andExpect(jsonPath("$.data[?(@.userName=='jules')]", hasSize(1)))
                .andExpect(jsonPath("$.data[?(@.userName=='admin')]", hasSize(1)))
                .andReturn();

        /*
        Get href(location) of user resource - vincent
         */
        net.minidev.json.JSONArray jsonArray =  JsonPath.read(
                usersMvcResult.getResponse().getContentAsString(), "$.data[?(@.userName=='vincent')]._links.self.href"
        );
        String vincentHref = jsonArray.get(0).toString();

        /*
        Get user and check
         */
        restUtil.performGet(vincentHref, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(vincent.getUserName())))
                .andExpect(jsonPath("$.fullName", is(vincent.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", contains(RestUtil.USER_ROLE)));

        /*
        Modify user
         */
        ModifyUserDto vincentModified = restUtil.getModifyUser("newPass", "new FN",
                new String[] {RestUtil.ADMIN_ROLE, RestUtil.USER_ROLE});

        restUtil.userPut(vincentModified, vincentHref, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(vincent.getUserName())))
                .andExpect(jsonPath("$.fullName", is(vincentModified.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(vincentModified.getRoles().length)))
                .andExpect(jsonPath("$.roles", Matchers.containsInAnyOrder(RestUtil.ADMIN_ROLE, RestUtil.USER_ROLE)));

        /*
        Get user and check new values again
         */
        restUtil.performGet(vincentHref, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(vincent.getUserName())))
                .andExpect(jsonPath("$.fullName", is(vincentModified.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(vincentModified.getRoles().length)))
                .andExpect(jsonPath("$.roles", Matchers.containsInAnyOrder(RestUtil.ADMIN_ROLE, RestUtil.USER_ROLE)));

        /*
        Login with new user password
         */
        restUtil.getAccessToken(vincent.getUserName(), vincentModified.getPassword());

        /*
        Delete user
         */
        restUtil.userDelete(vincentHref, adminToken)
                .andExpect(status().isOk());

        /*
        Get user - 404
         */
        restUtil.performGet(vincentHref, adminToken)
                .andExpect(status().isNotFound());

        /*
        List users
         */

        /*
        Login with vincent user's credentials.
        Oauth authentication returns:
            200 - Login successful
            401 - Bad CLIENT credentials
            400 - Bad USER credentials
         */
        restUtil.getAccessTokenResultActions(vincent.getUserName(), vincent.getPassword())
                .andExpect(status().isBadRequest());

        restUtil.performGet(usersHref, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[?(@.userName=='jules')]", hasSize(1)))
                .andExpect(jsonPath("$.data[?(@.userName=='admin')]", hasSize(1)))
                .andReturn();
    }

}
