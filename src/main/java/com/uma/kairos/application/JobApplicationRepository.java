package com.umasankar.applytrack.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID>, JpaSpecificationExecutor<JobApplication> {

    @Modifying
    @Query(value = "UPDATE job_applications SET user_id = (SELECT id FROM users ORDER BY created_at ASC, id ASC LIMIT 1) WHERE user_id IS NULL",
           nativeQuery = true)
    int claimOwnerlessApplications();

    java.util.Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);

    @Query("select a.status as status, count(a) as total from JobApplication a where a.userId = :userId group by a.status")
    List<StatusCount> countGroupedByStatus(UUID userId);

    interface StatusCount {
        ApplicationStatus getStatus();
        long getTotal();
    }
}
