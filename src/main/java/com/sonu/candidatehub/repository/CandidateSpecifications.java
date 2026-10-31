package com.sonu.candidatehub.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sonu.candidatehub.domain.Candidate;
import com.sonu.candidatehub.dto.CandidateFilter;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class CandidateSpecifications {

    private CandidateSpecifications() {
        // utility class — never instantiated
    }

    public static Specification<Candidate> from(CandidateFilter filter) {
        return (root, query, cb) -> {
            // Prevent duplicate rows when joining to the skills collection
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (hasText(filter.q())) {
                String likePattern = "%" + escapeLike(filter.q().toLowerCase()) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern, '\\'),
                    cb.like(cb.lower(root.get("lastName")), likePattern, '\\'),
                    cb.like(cb.lower(root.get("email")), likePattern, '\\'),
                    cb.like(cb.lower(root.get("headline")), likePattern, '\\'),
                    cb.like(cb.lower(root.get("currentCompany")), likePattern, '\\')
                ));
            }

            if (hasText(filter.location())) {
                predicates.add(cb.equal(cb.lower(root.get("location")), filter.location().toLowerCase()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.minExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceYears"), filter.minExperience()));
            }

            if (filter.maxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceYears"), filter.maxExperience()));
            }

            if (hasText(filter.currentCompany())) {
                predicates.add(cb.equal(cb.lower(root.get("currentCompany")), filter.currentCompany().toLowerCase()));
            }

            if (filter.maxNoticePeriod() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("noticePeriodDays"), filter.maxNoticePeriod()));
            }

            if (hasText(filter.source())) {
                predicates.add(cb.equal(cb.lower(root.get("source")), filter.source().toLowerCase()));
            }

            if (filter.skills() != null && !filter.skills().isEmpty()) {
                if (filter.matchAllSkills()) {
                    // AND semantics: candidate must have EVERY listed skill.
                    // One join + equality predicate per skill.
                    for (String skill : filter.skills()) {
                        predicates.add(cb.equal(
                            root.join("skills", JoinType.INNER),
                            skill.toLowerCase()
                        ));
                    }
                } else {
                    // OR semantics: candidate must have AT LEAST ONE listed skill.
                    predicates.add(
                        root.join("skills", JoinType.INNER)
                            .in(filter.skills().stream().map(String::toLowerCase).toList())
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String escapeLike(String s) {
        // Escape SQL LIKE wildcards so user input can't inject unintended patterns
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}