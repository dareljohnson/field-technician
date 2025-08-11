package com.technican.restservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testLogin() throws Exception {
        // Register admin
        String adminUsername = "admin_" + System.currentTimeMillis();
        Map<String, Object> admin = Map.of(
                "username", adminUsername,
                "password", "adminpass",
                "roles", new String[]{"ADMIN"}
        );
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isCreated());

        // Login admin
        Map<String, Object> adminLogin = Map.of(
                "username", adminUsername,
                "password", "adminpass"
        );
        String adminToken = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(adminToken).get("token").asText();

        // Register technician with admin token
        String techUsername = "loginuser_" + System.currentTimeMillis();
        Map<String, Object> user = Map.of(
                "username", techUsername,
                "password", "loginpass",
                "roles", new String[]{"TECHNICIAN"}
        );
        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        // Now, login as technician
        Map<String, Object> login = Map.of(
                "username", techUsername,
                "password", "loginpass"
        );
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
