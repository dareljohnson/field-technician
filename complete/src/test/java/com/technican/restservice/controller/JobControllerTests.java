package com.technican.restservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private com.technican.restservice.repository.UserRepositoryJpa userRepository;
    private String adminToken;
    private String techToken;
    private Long techUserId;
    private Long customerUserId;
    private String customerUsername;

    @BeforeEach
    public void setup() throws Exception {
        userRepository.deleteAll();
        // Register admin with unique username
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
        Map<String, Object> login = Map.of(
                "username", adminUsername,
                "password", "adminpass"
        );
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Register technician with unique username and admin token
        String techUsername = "tech_" + System.currentTimeMillis();
        Map<String, Object> tech = Map.of(
                "username", techUsername,
                "password", "techpass",
                "roles", new String[]{"TECHNICIAN"}
        );
        MvcResult techRegResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tech)))
                .andExpect(status().isCreated())
                .andReturn();
        techUserId = objectMapper.readTree(techRegResult.getResponse().getContentAsString()).get("id").asLong();
        // Login technician
        Map<String, Object> techLogin = Map.of(
                "username", techUsername,
                "password", "techpass"
        );
        MvcResult techResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(techLogin)))
                .andExpect(status().isOk())
                .andReturn();
        techToken = objectMapper.readTree(techResult.getResponse().getContentAsString()).get("token").asText();

        // Register customer with unique username and admin token
        customerUsername = "customer_" + System.currentTimeMillis();
        Map<String, Object> customer = Map.of(
                "username", customerUsername,
                "password", "customerpass",
                "roles", new String[]{"CUSTOMER"}
        );
        MvcResult customerRegResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andReturn();
        customerUserId = objectMapper.readTree(customerRegResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    public void testCreateJob() throws Exception {
        Map<String, Object> job = Map.of(
                "customerId", customerUserId,
                "serviceType", "AC Repair"
        );
        mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.serviceType").value("AC Repair"));
    }

    @Test
    public void testListJobs_Admin() throws Exception {
        mockMvc.perform(get("/jobs")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testListJobs_TechnicianForbidden() throws Exception {
        mockMvc.perform(get("/jobs")
                .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAssignJobToNonExistentTechnician() throws Exception {
        // Create a job as admin
        Map<String, Object> job = Map.of(
                "customerId", customerUserId,
                "serviceType", "AC Repair"
        );
        MvcResult jobResult = mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andReturn();
        Long jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();
        // Try to assign to a non-existent technician (id 9999)
        Map<String, Object> assignPayload = Map.of("technicianId", 9999L);
        mockMvc.perform(post("/jobs/" + jobId + "/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Technician does not exist or does not have TECHNICIAN role"));
    }

    @Test
    public void testAssignJobToNonTechnicianUser() throws Exception {
        // Create a job as admin
        Map<String, Object> job = Map.of(
                "customerId", customerUserId,
                "serviceType", "AC Repair"
        );
        MvcResult jobResult = mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andReturn();
        Long jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();
        // Try to assign to a customer (not a technician)
        Map<String, Object> assignPayload = Map.of("technicianId", customerUserId);
        mockMvc.perform(post("/jobs/" + jobId + "/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Technician does not exist or does not have TECHNICIAN role"));
    }

    @Test
    public void testTechnicianCanSeeAssignedJobs() throws Exception {
        // Create a job as admin
        Map<String, Object> job = Map.of(
                "customerId", customerUserId,
                "serviceType", "AC Repair"
        );
        MvcResult jobResult = mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andReturn();
        Long jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();
        // Assign job to technician
        Map<String, Object> assignPayload = Map.of("technicianId", techUserId);
        mockMvc.perform(post("/jobs/" + jobId + "/assign")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignPayload)))
                .andExpect(status().isOk());
        // Technician should see the job in /jobs/my
        mockMvc.perform(get("/jobs/my")
                .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(jobId));
    }

    @Test
    public void testCustomerCanSeeOwnJobs() throws Exception {
        // Create a job as admin for the customer
        Map<String, Object> job = Map.of(
                "customerId", customerUserId,
                "serviceType", "Heater Repair"
        );
        MvcResult jobResult = mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andReturn();
        Long jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asLong();
        // Customer logs in
        Map<String, Object> customerLogin = Map.of(
                "username", customerUsername,
                "password", "customerpass"
        );
        String customerToken = null;
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerLogin)))
                .andExpect(status().isOk())
                .andReturn();
        customerToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        mockMvc.perform(get("/jobs/my")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(jobId));
    }
}
