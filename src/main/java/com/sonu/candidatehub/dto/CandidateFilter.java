package com.sonu.candidatehub.dto;

import com.sonu.candidatehub.domain.CandidateStatus;

import java.util.Set;

public record CandidateFilter(
    String q,
    Set<String> skills,
    boolean matchAllSkills,
    String location,
    CandidateStatus status,
    Integer minExperience,
    Integer maxExperience,
    String currentCompany,
    Integer maxNoticePeriod,
    String source
) {}