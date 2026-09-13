package com.umasankar.applytrack.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID>, JpaSpecificationExecutor<JobApplication> {

    long countByStatus(ApplicationStatus status);

    @Query("select a.status as status, count(a) as total from JobApplication a group by a.status")
    List<StatusCount> countGroupedByStatus();

    interface StatusCount {
        ApplicationStatus getStatus();
        long getTotal();
    }
}
