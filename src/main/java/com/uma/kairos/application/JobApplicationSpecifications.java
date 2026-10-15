package com.umasankar.applytrack.application;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

public final class JobApplicationSpecifications {

    private JobApplicationSpecifications() {
    }

    public static Specification<JobApplication> matches(UUID userId, String search, ApplicationStatus status,
                                                         ApplicationSource source, String location,
                                                         LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userId"), userId));

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("jobTitle")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), pattern)
                ));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (source != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("applicationSource"), source));
            }
            if (StringUtils.hasText(location)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("location")), location.trim().toLowerCase()));
            }
            if (fromDate != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("applicationDate"), fromDate));
            }
            if (toDate != null) {
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get("applicationDate"), toDate));
            }
            return predicate;
        };
    }
}
