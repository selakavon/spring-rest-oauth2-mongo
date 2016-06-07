package sixkiller.sample.restapi.controller;

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
import sixkiller.sample.JoggingApplication;
import sixkiller.sample.domain.User;
import sixkiller.sample.repository.TestTimeEntryRepository;
import sixkiller.sample.repository.TestUserRepository;
import sixkiller.sample.repository.TimeEntryRepository;
import sixkiller.sample.repository.UserRepository;
import sixkiller.sample.restapi.dto.CreateUserDto;
import sixkiller.sample.restapi.dto.ModifyUserDto;
import sixkiller.sample.security.RepositoryUserDetailsService;
import sixkiller.sample.service.UserService;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ala on 14.5.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {JoggingApplication.class})
public class UserRestControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryUserDetailsService repositoryUserDetailsService;

    private MockMvc mockMvc;

    private RestUtil restUtil;

    private User adminUser;

    @Before
    public void setUp() {
        UserRepository userRepository = new TestUserRepository();
        TimeEntryRepository timeEntryRepository = new TestTimeEntryRepository();

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(springSecurityFilterChain).build();
        restUtil = new RestUtil(mockMvc);
        userService.setUserRepository(userRepository);
        userService.setTimeEntryRepository(timeEntryRepository);
        repositoryUserDetailsService.setUserRepository(userRepository);

        adminUser = restUtil.saveAdminUser(userRepository);
    }

    @Test
    public void unauthenticatedTest() throws Exception {
        MvcResult mvcResult = restUtil.userPost(restUtil.getCreateUser("user")).andExpect(status().isCreated())
                .andReturn();
        String userLocation = restUtil.getLocation(mvcResult);

        restUtil.usersGet("").andExpect(status().isUnauthorized());
        restUtil.performGet(userLocation, "").andExpect(status().isUnauthorized());
        restUtil.userDelete(userLocation, "").andExpect(status().isUnauthorized());

        restUtil.userPut(
                restUtil.getModifyUser("pass", "FN", new String[]{"ADMIN"}), userLocation, ""
        ).andExpect(status().isUnauthorized());

    }

    @Test
    public void validationTest() throws Exception {
        CreateUserDto user = restUtil.getCreateUser("user");
        user.setUserName(null);
        restUtil.userPost(user).andExpect(status().isBadRequest());

        user = restUtil.getCreateUser("user");
        user.setFullName(null);
        restUtil.userPost(user).andExpect(status().isBadRequest());

        user = restUtil.getCreateUser("user");
        user.setPassword(null);
        restUtil.userPost(user).andExpect(status().isBadRequest());

        user = restUtil.getCreateUser("user");
        user.setPassword("");
        restUtil.userPost(user).andExpect(status().isBadRequest());

        ModifyUserDto modifyUser = getValidModifyUser();
        modifyUser.setFullName(null);
        restUtil.userPost(user).andExpect(status().isBadRequest());

        modifyUser = getValidModifyUser();
        modifyUser.setFullName("");
        restUtil.userPost(user).andExpect(status().isBadRequest());
    }

    public void changeUserKeepPasswordTest() throws Exception {
        CreateUserDto user = restUtil.getCreateUser("user");
        restUtil.userPost(user).andExpect(status().isCreated()).andReturn();

        /*
        Try to login.
         */
        restUtil.getAccessToken(user.getUserName(), user.getPassword());

        ModifyUserDto modifyUser = getValidModifyUser();
        modifyUser.setPassword(null);
        restUtil.userPost(user).andExpect(status().isOk());

        modifyUser = getValidModifyUser();
        modifyUser.setPassword("");
        restUtil.userPost(user).andExpect(status().isOk());

        /*
        Try to login.
         */
        restUtil.getAccessToken(user.getUserName(), user.getPassword());
    }

    private ModifyUserDto getValidModifyUser() {
        return restUtil.getModifyUser("pass", "FN", new String[] {"USER"});
    }

    @Test
    public void postConflictTest() throws Exception {
        restUtil.userPost(restUtil.getCreateUser("user")).andExpect(status().isCreated());
        restUtil.userPost(restUtil.getCreateUser("user")).andExpect(status().isConflict());
    }

    @Test
    public void authenticatedTest() throws Exception {
        CreateUserDto user = restUtil.getCreateUser("user");
        CreateUserDto user2 = restUtil.getCreateUser("user2");

        String userLocation = restUtil.getLocation(
            restUtil.userPost(user).andExpect(status().isCreated())
                .andReturn()
        );

        String user2Location = restUtil.getLocation(
                restUtil.userPost(user2).andExpect(status().isCreated())
                        .andReturn()
        );

        String userToken = restUtil.getAccessToken(user.getUserName(), user.getPassword());

        restUtil.performGet(user2Location, userToken)
                .andExpect(status().isForbidden());

        restUtil.performGet(userLocation, userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(user.getUserName())))
                .andExpect(jsonPath("$.fullName", is(user.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", contains("USER")));

        restUtil.usersGet(userToken).andExpect(status().isForbidden());
        restUtil.userPut(
                restUtil.getModifyUser("pass", "FN", new String[]{"ADMIN"}), userLocation, userToken
        ).andExpect(status().isForbidden());
    }

    @Test
    public void adminTest() throws Exception {
        String adminToken = restUtil.getAccessToken(adminUser.getUserName(), adminUser.getPassword());

        CreateUserDto user = restUtil.getCreateUser("user");
        String userLocation = restUtil.getLocation(
                restUtil.userPost(user).andExpect(status().isCreated())
                        .andReturn()
        );

        restUtil.performGet(userLocation, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(user.getUserName())))
                .andExpect(jsonPath("$.fullName", is(user.getFullName())))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", contains(RestUtil.DEFAULT_ROLE)));

        String newFullName = "New FN";
        String newPass = "newPass";
        String[] newRoles = {RestUtil.ADMIN_ROLE, RestUtil.USER_ROLE};

        restUtil.userPut(
                restUtil.getModifyUser(
                        newPass, newFullName, newRoles
                ), userLocation, adminToken
        );

        restUtil.performGet(userLocation, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName", is(user.getUserName())))
                .andExpect(jsonPath("$.fullName", is(newFullName)))
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", Matchers.containsInAnyOrder(RestUtil.ADMIN_ROLE, RestUtil.USER_ROLE)));

        restUtil.getAccessToken(user.getUserName(), newPass);

        restUtil.usersGet(adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        restUtil.userDelete(userLocation, adminToken).andExpect(status().isOk());

        restUtil.usersGet(adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        restUtil.performGet(userLocation, adminToken).andExpect(status().isNotFound());
    }

}
