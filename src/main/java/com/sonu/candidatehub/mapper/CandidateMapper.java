package com.sonu.candidatehub.mapper;

import com.sonu.candidatehub.domain.Candidate;
import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateRequest;
import com.sonu.candidatehub.dto.CandidateResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CandidateMapper {

    public Candidate toEntity(CandidateRequest request) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(request.firstName());
        candidate.setLastName(request.lastName());
        candidate.setEmail(normalizeEmail(request.email()));
        candidate.setPhone(request.phone());
        candidate.setLocation(request.location());
        candidate.setCurrentCompany(request.currentCompany());
        candidate.setCurrentJobTitle(request.currentJobTitle());
        candidate.setHeadline(request.headline());
        candidate.setExperienceYears(request.experienceYears());
        candidate.setNoticePeriodDays(request.noticePeriodDays());
        candidate.setHighestEducation(request.highestEducation());
        candidate.setStatus(request.status() != null ? request.status() : CandidateStatus.NEW);
        candidate.setSource(request.source());
        candidate.setResumeUrl(request.resumeUrl());
        candidate.setNotes(request.notes());
        candidate.setSkills(normalizeSkills(request.skills()));
        return candidate;
    }

    /**
     * Applies request fields onto an existing managed entity (used for updates).
     * Deliberately does NOT touch id, version, createdAt, updatedAt — those are
     * either immutable or managed automatically by JPA/Hibernate.
     */
    public void updateEntity(Candidate candidate, CandidateRequest request) {
        candidate.setFirstName(request.firstName());
        candidate.setLastName(request.lastName());
        candidate.setEmail(normalizeEmail(request.email()));
        candidate.setPhone(request.phone());
        candidate.setLocation(request.location());
        candidate.setCurrentCompany(request.currentCompany());
        candidate.setCurrentJobTitle(request.currentJobTitle());
        candidate.setHeadline(request.headline());
        candidate.setExperienceYears(request.experienceYears());
        candidate.setNoticePeriodDays(request.noticePeriodDays());
        candidate.setHighestEducation(request.highestEducation());
        if (request.status() != null) {
            candidate.setStatus(request.status());
        }
        candidate.setSource(request.source());
        candidate.setResumeUrl(request.resumeUrl());
        candidate.setNotes(request.notes());
        candidate.setSkills(normalizeSkills(request.skills()));
    }
    
    public void patchEntity(Candidate candidate, CandidateRequest request) {
        if (request.firstName() != null) candidate.setFirstName(request.firstName());
        if (request.lastName() != null) candidate.setLastName(request.lastName());
        if (request.email() != null) candidate.setEmail(normalizeEmail(request.email()));
        if (request.phone() != null) candidate.setPhone(request.phone());
        if (request.location() != null) candidate.setLocation(request.location());
        if (request.currentCompany() != null) candidate.setCurrentCompany(request.currentCompany());
        if (request.currentJobTitle() != null) candidate.setCurrentJobTitle(request.currentJobTitle());
        if (request.headline() != null) candidate.setHeadline(request.headline());
        if (request.experienceYears() != null) candidate.setExperienceYears(request.experienceYears());
        if (request.noticePeriodDays() != null) candidate.setNoticePeriodDays(request.noticePeriodDays());
        if (request.highestEducation() != null) candidate.setHighestEducation(request.highestEducation());
        if (request.status() != null) candidate.setStatus(request.status());
        if (request.source() != null) candidate.setSource(request.source());
        if (request.resumeUrl() != null) candidate.setResumeUrl(request.resumeUrl());
        if (request.notes() != null) candidate.setNotes(request.notes());
        if (request.skills() != null) candidate.setSkills(normalizeSkills(request.skills()));
    }

    public CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
            candidate.getId(),
            candidate.getFirstName(),
            candidate.getLastName(),
            candidate.getEmail(),
            candidate.getPhone(),
            candidate.getLocation(),
            candidate.getCurrentCompany(),
            candidate.getCurrentJobTitle(),
            candidate.getHeadline(),
            candidate.getExperienceYears(),
            candidate.getNoticePeriodDays(),
            candidate.getHighestEducation(),
            candidate.getStatus(),
            candidate.getSource(),
            candidate.getResumeUrl(),
            candidate.getNotes(),
            candidate.getSkills(),
            candidate.getCreatedAt(),
            candidate.getUpdatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        if (skills == null) return new HashSet<>();
        Set<String> normalized = new HashSet<>();
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                normalized.add(skill.trim().toLowerCase());
            }
        }
        return normalized;
    }
}