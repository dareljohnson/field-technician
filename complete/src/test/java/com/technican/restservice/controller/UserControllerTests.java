package com.technican.restservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private com.technican.restservice.repository.UserRepositoryJpa userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterUser() throws Exception {
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        Map<String, Object> user = Map.of(
                "username", uniqueUsername,
                "password", "testpass",
                "roles", new String[]{"ADMIN"}
        );
        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        if (node.get("username") == null) {
            throw new AssertionError("No 'username' in response. Full response: " + response);
        }
        String actualUsername = node.get("username").asText();
        if (!uniqueUsername.equals(actualUsername)) {
            throw new AssertionError("Expected username: " + uniqueUsername + ", but got: " + actualUsername + ". Full response: " + response);
        }
    }

    @Test
    public void testDeleteUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());
    }
}
