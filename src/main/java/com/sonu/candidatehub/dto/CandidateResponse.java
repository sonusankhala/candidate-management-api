package com.sonu.candidatehub.dto;

import com.sonu.candidatehub.domain.CandidateStatus;

import java.time.Instant;
import java.util.Set;

public record CandidateResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    String location,
    String currentCompany,
    String currentJobTitle,
    String headline,
    Integer experienceYears,
    Integer noticePeriodDays,
    String highestEducation,
    CandidateStatus status,
    String source,
    String resumeUrl,
    String notes,
    Set<String> skills,
    Instant createdAt,
    Instant updatedAt
) {}