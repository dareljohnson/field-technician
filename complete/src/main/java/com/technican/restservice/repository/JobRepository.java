package com.technican.restservice.repository;

import com.technican.restservice.model.Job;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class JobRepository {
    private final Map<Long, Job> jobs = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Job save(Job job) {
        if (job.getId() == null) {
            job.setId(idGen.getAndIncrement());
        }
        jobs.put(job.getId(), job);
        return job;
    }

    public Optional<Job> findById(Long id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public List<Job> findAll() {
        return new ArrayList<>(jobs.values());
    }

    public void deleteById(Long id) {
        jobs.remove(id);
    }

    public void deleteAll() {
        jobs.clear();
    }
}
