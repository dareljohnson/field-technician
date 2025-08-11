package com.technican.restservice.repository;

import com.technican.restservice.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepositoryJpa extends JpaRepository<Job, Long> {
    List<Job> findByTechnicianId(Long technicianId);
    List<Job> findByCustomerId(Long customerId);
}
