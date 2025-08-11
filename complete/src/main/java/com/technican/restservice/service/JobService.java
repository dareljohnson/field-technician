package com.technican.restservice.service;

import com.technican.restservice.model.Job;
import com.technican.restservice.model.JobStatus;
import com.technican.restservice.repository.JobRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDateTime;

@Service
public class JobService {
    @Autowired
    private JobRepositoryJpa jobRepository;
    @Autowired
    private com.technican.restservice.service.UserService userService;

    public Job createJob(Job job) {
        // Input validation: customerId and serviceType must not be null/empty
        if (job.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (job.getServiceType() == null || job.getServiceType().isBlank()) {
            throw new IllegalArgumentException("serviceType is required");
        }
        job.setCreatedAt(LocalDateTime.now());
        job.setStatus(JobStatus.SCHEDULED);
        return jobRepository.save(job);
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public List<Job> findByTechnicianId(Long technicianId) {
        return jobRepository.findByTechnicianId(technicianId);
    }

    public List<Job> findByCustomerId(Long customerId) {
        return jobRepository.findByCustomerId(customerId);
    }

    public void updateJobStatus(Long id, JobStatus status) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(status);
            jobRepository.save(job);
        }
    }

    public void assignTechnician(Long jobId, Long technicianId) {
        // Validate technician exists and has TECHNICIAN role
        var userOpt = userService.findById(technicianId);
        if (userOpt.isEmpty() || userOpt.get().getRoles() == null ||
            userOpt.get().getRoles().stream().noneMatch(r -> r == com.technican.restservice.model.Role.TECHNICIAN)) {
            throw new IllegalArgumentException("Technician does not exist or does not have TECHNICIAN role");
        }
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setTechnicianId(technicianId);
            jobRepository.save(job);
        }
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
