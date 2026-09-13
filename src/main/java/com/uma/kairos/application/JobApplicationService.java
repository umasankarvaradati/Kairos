package com.umasankar.applytrack.application;

import com.umasankar.applytrack.common.api.PageResponse;
import com.umasankar.applytrack.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public JobApplicationResponse create(JobApplicationRequest request) {
        JobApplication application = new JobApplication(
            clean(request.companyName()), clean(request.jobTitle()), clean(request.jobUrl()), clean(request.location()),
            request.applicationSource(), request.status(), request.applicationDate(), clean(request.notes())
        );
        return JobApplicationResponse.from(repository.save(application));
    }

    public JobApplicationResponse get(UUID id) {
        return JobApplicationResponse.from(findById(id));
    }

    public PageResponse<JobApplicationResponse> findAll(String search, ApplicationStatus status,
                                                         ApplicationSource source, String location, LocalDate fromDate,
                                                         LocalDate toDate, Pageable pageable) {
        Page<JobApplication> result = repository.findAll(
            JobApplicationSpecifications.matches(search, status, source, location, fromDate, toDate), pageable);
        return PageResponse.from(result, JobApplicationResponse::from);
    }

    @Transactional
    public JobApplicationResponse update(UUID id, JobApplicationRequest request) {
        JobApplication application = findById(id);
        application.update(
            clean(request.companyName()), clean(request.jobTitle()), clean(request.jobUrl()), clean(request.location()),
            request.applicationSource(), request.status(), request.applicationDate(), clean(request.notes())
        );
        return JobApplicationResponse.from(application);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findById(id));
    }

    private JobApplication findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
