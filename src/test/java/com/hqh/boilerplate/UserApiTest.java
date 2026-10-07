package com.hqh.boilerplate;

import com.hqh.boilerplate.BoilerplateApplication;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BoilerplateApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerMissingFieldReturns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.local\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isArray());
    }

    @Test
    void registerThenLogin() throws Exception {
        register("Admin", "User", "apiadmin", "apiadmin@test.local", "quiz1234", "ROLE_ADMIN");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"apiadmin@test.local\",\"password\":\"quiz1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void duplicateRegisterReturns400() throws Exception {
        register("Admin", "User", "dupuser", "dup@test.local", "quiz1234", "ROLE_USER");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("Other", "User", "otheruser", "dup@test.local", "quiz1234", "ROLE_USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("USERNAME OR EMAIL ALREADY EXISTS"));
    }

    @Test
    void findWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/user/find/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleUserCannotReadAnotherUser() throws Exception {
        register("Admin", "User", "owneruser", "owner@test.local", "quiz1234", "ROLE_ADMIN");
        register("Plain", "User", "plainuser", "plain@test.local", "quiz1234", "ROLE_USER");
        String token = login("plain@test.local", "quiz1234");

        mockMvc.perform(get("/user/find/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCrud() throws Exception {
        register("Admin", "User", "crudadmin", "crudadmin@test.local", "quiz1234", "ROLE_ADMIN");
        String token = login("crudadmin@test.local", "quiz1234");

        MvcResult created = mockMvc.perform(post("/user/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"New","lastName":"User","username":"newuser","email":"new@test.local","roles":"ROLE_USER","isActive":true,"isNonLocked":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.password").isNotEmpty())
                .andReturn();
        int id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/user/list").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'newuser')]").exists());

        mockMvc.perform(get("/user/find/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/user/find/99999").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NO USER FOUND BY ID: 99999"));

        mockMvc.perform(post("/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentUsername":"newuser","firstName":"Updated","lastName":"Person","username":"newuser","email":"new@test.local","roles":"ROLE_USER","isActive":true,"isNonLocked":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));

        mockMvc.perform(post("/user/update")
                        .header("Authorization", "Bearer " + login("new@test.local", JsonPath.read(created.getResponse().getContentAsString(), "$.password")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentUsername":"newuser","firstName":"Updated","lastName":"Person","username":"newuser","email":"new@test.local","roles":"ROLE_USER","isActive":true,"isNonLocked":true}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/user/delete/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("USER DELETED SUCCESSFULLY"));

        mockMvc.perform(delete("/user/delete/99999").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private void register(String firstName, String lastName, String username, String email, String password, String roles)
            throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(firstName, lastName, username, email, password, roles)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private String registerBody(String firstName, String lastName, String username, String email, String password, String roles) {
        return """
                {"firstName":"%s","lastName":"%s","username":"%s","email":"%s","password":"%s","roles":"%s"}
                """.formatted(firstName, lastName, username, email, password, roles);
    }

}
