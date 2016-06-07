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
import sixkiller.sample.JoggingApplication;
import sixkiller.sample.domain.User;
import sixkiller.sample.repository.TestTimeEntryRepository;
import sixkiller.sample.repository.TestUserRepository;
import sixkiller.sample.repository.TimeEntryRepository;
import sixkiller.sample.repository.UserRepository;
import sixkiller.sample.restapi.ValidatingUserRepositoryDecorator;
import sixkiller.sample.restapi.dto.CreateUserDto;
import sixkiller.sample.restapi.dto.TimeEntryDto;
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
public class TimeEntryRestControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserService userService;

    @Autowired
    private TimeEntryRestController timeEntryRestController;

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
        timeEntryRestController.setValidatingUserRepositoryDecorator(validatingUserRepositoryDecorator);

        adminUser = restUtil.saveAdminUser(userRepository);
    }

    @Test
    public void unauthenticatedTest() throws Exception {
        CreateUserDto user = restUtil.getCreateUser("vincent");

        MvcResult mvcResult = restUtil.userPost(user).andExpect(status().isCreated())
                .andReturn();
        String timeEntriesHref = restUtil.getTimeEntriesHref(mvcResult);

        restUtil.performGet(timeEntriesHref, "")
                .andExpect(status().isUnauthorized());

        restUtil.performGet(timeEntriesHref + "/someId", "")
                .andExpect(status().isUnauthorized());

        restUtil.timeEntryPut(restUtil.getTimeEntryDto(20100101, 1, 1), timeEntriesHref + "/someId", "")
                .andExpect(status().isUnauthorized());

        restUtil.performGet(timeEntriesHref, "")
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void validationTest() throws Exception {
        CreateUserDto user = restUtil.getCreateUser("user");
        MvcResult userMvcResult = restUtil.userPost(user).andExpect(status().isCreated())
                .andReturn();
        String userTimeEntriesHref = restUtil.getTimeEntriesHref(
                userMvcResult);
        String userToken = restUtil.getAccessToken(user.getUserName(), user.getPassword());

        restUtil.timeEntryPost(getValidTimeEntryDto(), userTimeEntriesHref, userToken)
                .andExpect(status().isCreated());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDate(20160431), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDate(1), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDate(-1), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDate(null), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDistance(0D), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDistance(-1D), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withDistance(null), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withTimeSeconds(0), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withTimeSeconds(-1), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());

        restUtil.timeEntryPost(getValidTimeEntryDto().withTimeSeconds(null), userTimeEntriesHref, userToken)
                .andExpect(status().isBadRequest());
    }

    private TimeEntryDto getValidTimeEntryDto() {
        return restUtil.getTimeEntryDto(20160101, 1, 1);
    }

    @Test
    public void authenticatedTest() throws Exception {

        CreateUserDto vincent = restUtil.getCreateUser("vincent");
        CreateUserDto jules = restUtil.getCreateUser("jules");

        MvcResult vincentMvcResult = restUtil.userPost(vincent).andExpect(status().isCreated())
                .andReturn();

        String vincentLocation = restUtil.getLocation(vincentMvcResult);
        String vincentToken = restUtil.getAccessToken(vincent.getUserName(), vincent.getPassword());

        String vincentTimeEntriesHref = restUtil.getTimeEntriesHref(
                restUtil.performGet(vincentLocation, vincentToken)
                        .andExpect(status().isOk())
                        .andReturn()
        );

        restUtil.userPost(jules).andExpect(status().isCreated())
                .andReturn();
        String julesToken = restUtil.getAccessToken(jules.getUserName(), jules.getPassword());


        restUtil.performGet(vincentTimeEntriesHref, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        TimeEntryDto vincentEntry = restUtil.getTimeEntryDto(20100225, 12.5, 7232);
        String entryLocation = restUtil.postAndGetLocation(vincentToken, vincentTimeEntriesHref, vincentEntry);

        restUtil.performGet(entryLocation, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is(vincentEntry.getDate())))
                .andExpect(jsonPath("$.distance", is(vincentEntry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(vincentEntry.getTimeSeconds())));

        TimeEntryDto vincentEntry2 = restUtil.getTimeEntryDto(20100225, 23, 12232);
        String entry2Location = restUtil.postAndGetLocation(vincentToken, vincentTimeEntriesHref, vincentEntry2);

        restUtil.performGet(vincentTimeEntriesHref, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        TimeEntryDto vincentEntry2Modified = restUtil.getTimeEntryDto(20150325, 2.5, 612);
        restUtil.timeEntryPut(vincentEntry2Modified, entry2Location, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date", is(vincentEntry2Modified.getDate())))
                .andExpect(jsonPath("$.distance", is(vincentEntry2Modified.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(vincentEntry2Modified.getTimeSeconds())));

        restUtil.performGet(vincentTimeEntriesHref, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        restUtil.timeEntryDelete(entryLocation, vincentToken)
                .andExpect(status().isOk());

        restUtil.performGet(vincentTimeEntriesHref, vincentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        restUtil.performGet(entryLocation, vincentToken)
                .andExpect(status().isNotFound());

        restUtil.performGet(entry2Location, vincentToken)
                .andExpect(status().isOk());

        /*
        JULES should be forbidden to access Vincent's jogging records.
         */

        restUtil.performGet(vincentTimeEntriesHref, julesToken)
                .andExpect(status().isForbidden());

        restUtil.performGet(entry2Location, julesToken)
                .andExpect(status().isForbidden());

        restUtil.timeEntryPut(vincentEntry2Modified, entry2Location, julesToken)
                .andExpect(status().isForbidden());

        restUtil.timeEntryDelete(entry2Location, julesToken)
                .andExpect(status().isForbidden());

        /*
        ADMIN should be allowed to access anything.
         */
        String adminToken = restUtil.getAccessToken(adminUser.getUserName(), adminUser.getPassword());

        restUtil.performGet(vincentTimeEntriesHref, adminToken)
                .andExpect(status().isOk());

        restUtil.performGet(entry2Location, adminToken)
                .andExpect(status().isOk());

        restUtil.timeEntryPut(vincentEntry2Modified, entry2Location, adminToken)
                .andExpect(status().isOk());

        restUtil.timeEntryDelete(entry2Location, adminToken)
                .andExpect(status().isOk());

    }

}
