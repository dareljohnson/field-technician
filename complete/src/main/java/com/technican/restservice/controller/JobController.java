package com.technican.restservice.controller;

import com.technican.restservice.model.Job;
import com.technican.restservice.model.JobStatus;
import com.technican.restservice.security.JwtUtil;
import com.technican.restservice.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/jobs")
public class JobController {
    @Autowired
    private JobService jobService;

    @Autowired
    private JwtUtil jwtUtil;


    // Create a new job (admin, technician, scheduler)
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        Long userId;
        try {
            roles = jwtUtil.getRolesFromToken(token);
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        if (!(roles.contains("ADMIN") || roles.contains("TECHNICIAN") || roles.contains("SCHEDULER"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to create jobs"));
        }
        // Only admin, scheduler can create jobs for any customer; technician can only create for themselves
        if (roles.contains("TECHNICIAN") && !roles.contains("ADMIN") && !roles.contains("SCHEDULER")) {
            job.setTechnicianId(userId);
        }
        try {
            Job created = jobService.createJob(job);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // List all jobs (admin, scheduler only)
    @GetMapping
    public ResponseEntity<?> listJobs(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        try {
            roles = jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        if (!(roles.contains("ADMIN") || roles.contains("SCHEDULER"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to list all jobs"));
        }
        return ResponseEntity.ok(jobService.findAll());
    }

    // Technician: view assigned jobs; Customer: view their jobs/requests
    @GetMapping("/my")
    public ResponseEntity<?> myJobs(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        Long userId;
        try {
            roles = jwtUtil.getRolesFromToken(token);
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        if (roles.contains("TECHNICIAN")) {
            return ResponseEntity.ok(jobService.findByTechnicianId(userId));
        } else if (roles.contains("CUSTOMER")) {
            return ResponseEntity.ok(jobService.findByCustomerId(userId));
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }
    }


    // Update job status (admin, scheduler, assigned technician)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id, @RequestBody Map<String, String> req, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        Long userId;
        try {
            roles = jwtUtil.getRolesFromToken(token);
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        Job job = jobService.findById(id).orElse(null);
        if (job == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Job not found"));
        }
        if (!(roles.contains("ADMIN") || roles.contains("SCHEDULER") || (roles.contains("TECHNICIAN") && Objects.equals(job.getTechnicianId(), userId)))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to update job status"));
        }
        String status = req.get("status");
        JobStatus newStatus;
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        try {
            newStatus = JobStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value"));
        }
        jobService.updateJobStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("status", newStatus));
    }

    // Assign a technician to a job (admin, scheduler)
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignTechnician(@PathVariable Long id, @RequestBody Map<String, Long> req, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        try {
            roles = jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        if (!(roles.contains("ADMIN") || roles.contains("SCHEDULER"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to assign technician"));
        }
        Long techId = req.get("technicianId");
        if (techId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "technicianId is required"));
        }
        try {
            jobService.assignTechnician(id, techId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("assigned", techId));
    }

    // Delete a job (admin, scheduler only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        String token = authHeader.substring(7);
        Set<String> roles;
        try {
            roles = jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        if (!(roles.contains("ADMIN") || roles.contains("SCHEDULER"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized to delete job"));
        }
        jobService.deleteJob(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
