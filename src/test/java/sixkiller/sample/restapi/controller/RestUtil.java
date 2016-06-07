package sixkiller.sample.restapi.controller;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Base64Utils;
import sixkiller.sample.domain.User;
import sixkiller.sample.repository.UserRepository;
import sixkiller.sample.restapi.dto.CreateUserDto;
import sixkiller.sample.restapi.dto.ModifyUserDto;
import sixkiller.sample.restapi.dto.TimeEntryDto;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ala on 15.5.16.
 */
public class RestUtil {

    public static final String CLIENT_ID = "webui";
    public static final String CLIENT_SECRET = "webuisecret";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    public static final String DEFAULT_ROLE = "USER";
    public static final String REST_USERS = "/api/users";

    MockMvc mockMvc;

    private Gson gson = new Gson();

    public RestUtil(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions getAccessTokenResultActions(String username, String password) throws Exception {
        String authorization = "Basic "
                + new String(Base64Utils.encode((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()));

        return mockMvc
                .perform(
                        post("/oauth/token")
                                .header("Authorization", authorization)
                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", username)
                                .param("password", password)
                                .param("grant_type", "password")
                                .param("scope", "read write")
                                .param("client_id", CLIENT_ID)
                                .param("client_secret", CLIENT_SECRET));

    }

    public String getAccessToken(String username, String password) throws Exception {
        String content = getAccessTokenResultActions(username, password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", is(notNullValue())))
                .andExpect(jsonPath("$.token_type", is(equalTo("bearer"))))
                .andExpect(jsonPath("$.refresh_token", is(notNullValue())))
                .andExpect(jsonPath("$.expires_in", is(greaterThan(4000))))
                .andExpect(jsonPath("$.scope", is(equalTo("read write"))))
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(content, "$.access_token");
    }

    public ResultActions userPut(ModifyUserDto user, String userHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(put(userHref), accessToken).content(
                gson.toJson(user)
        ).contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions userPost(CreateUserDto user) throws Exception {
        return mockMvc.perform(post(REST_USERS).content(
                gson.toJson(user)
        ).contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions usersGet(String accessToken) throws Exception {
        return mockMvc.perform(authorize(get(REST_USERS), accessToken));
    }

    public ResultActions userDelete(String userHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(delete(userHref), accessToken));
    }

    public MockHttpServletRequestBuilder authorize(MockHttpServletRequestBuilder requestBuilder, String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return requestBuilder;
        }
        return requestBuilder.header("Authorization", "Bearer " + accessToken);
    }

    public ResultActions performGet(String resourceHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(get(resourceHref), accessToken));
    }

    public ResultActions timeEntryPost(TimeEntryDto timeEntry, String timeEntriesHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(post(timeEntriesHref), accessToken).content(
                gson.toJson(timeEntry)
        ).contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions timeEntryPut(TimeEntryDto timeEntry, String timeEntryHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(put(timeEntryHref), accessToken).content(
                gson.toJson(timeEntry)
        ).contentType(MediaType.APPLICATION_JSON));
    }
    public ResultActions timeEntryDelete(String timeEntryHref, String accessToken) throws Exception {
        return mockMvc.perform(authorize(delete(timeEntryHref), accessToken));
    }


    public String postAndGetLocation(String accessToken, String timeEntriesHref, TimeEntryDto timeEntry) throws Exception {
        MvcResult entryResult = timeEntryPost(
                timeEntry, timeEntriesHref, accessToken
        ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.date", is(timeEntry.getDate())))
                .andExpect(jsonPath("$.distance", is(timeEntry.getDistance())))
                .andExpect(jsonPath("$.timeSeconds", is(timeEntry.getTimeSeconds())))
                .andReturn();

        return getLocation(entryResult);
    }

    public TimeEntryDto getTimeEntryDto(int date, double distance, int timeSeconds) {
        TimeEntryDto timeEntry = new TimeEntryDto();

        timeEntry.setDate(date);
        timeEntry.setDistance(distance);
        timeEntry.setTimeSeconds(timeSeconds);

        return timeEntry;
    }

    public CreateUserDto getCreateUser(String userName) {
        CreateUserDto user = new CreateUserDto();
        user.setPassword(userName + "pass");
        user.setFullName("Mr. " + userName);
        user.setUserName(userName);

        return user;
    }

    public ModifyUserDto getModifyUser(String password, String fullName, String[] roles) {
        ModifyUserDto user = new ModifyUserDto();
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRoles(roles);

        return user;
    }

    public User getUser(String userName, String fullName, String password, String[] roles) {
        User user = new User();

        user.setUserName(userName);
        user.setFullName(fullName);
        user.setPassword(password);
        user.setRoles(roles);

        return user;
    }

    public User saveAdminUser(UserRepository userRepository) {
        User adminUser = getAdminUser();
        adminUser.setPassword(
                new BCryptPasswordEncoder().encode(adminUser.getPassword())
        );

        userRepository.save(adminUser);

        return getAdminUser();
    }

    public User getAdminUser() {
        return getUser("admin", "Administrator", "adminPass", new String[] {ADMIN_ROLE});
    }

    public String getLocation(MvcResult mvcResult) {
        return mvcResult.getResponse().getHeader("location");
    }

    public String getTimeEntriesHref(MvcResult mvcResult) throws UnsupportedEncodingException {
        return getResourceHref(mvcResult, "$._links.timeEntries.href");
    }
    public String getReportWeeksHref(MvcResult mvcResult) throws UnsupportedEncodingException {
        return getResourceHref(mvcResult, "$._links.reportWeeks.href");
    }

    public String getUsersHref(MvcResult mvcResult) throws UnsupportedEncodingException {
        return getResourceHref(mvcResult, "$._links.users.href");
    }


    public String getResourceHref(MvcResult mvcResult, String jsonPath) throws UnsupportedEncodingException {
        return JsonPath.read(
                mvcResult.getResponse().getContentAsString(),
                jsonPath
        );
    }
}
