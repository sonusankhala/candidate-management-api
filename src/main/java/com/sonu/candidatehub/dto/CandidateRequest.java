package com.sonu.candidatehub.dto;

import com.sonu.candidatehub.domain.CandidateStatus;
import jakarta.validation.constraints.*;

import java.util.Set;

public record CandidateRequest(

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Pattern(regexp = "^[+]?[0-9\\- ]{7,20}$", message = "Phone number format is invalid")
    String phone,

    @Size(max = 100, message = "Location must not exceed 100 characters")
    String location,

    @Size(max = 150, message = "Current company must not exceed 150 characters")
    String currentCompany,

    @Size(max = 150, message = "Current job title must not exceed 150 characters")
    String currentJobTitle,

    @Size(max = 200, message = "Headline must not exceed 200 characters")
    String headline,

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 60, message = "Experience years seems unrealistic")
    Integer experienceYears,

    @Min(value = 0, message = "Notice period cannot be negative")
    @Max(value = 365, message = "Notice period seems unrealistic")
    Integer noticePeriodDays,

    @Size(max = 100, message = "Highest education must not exceed 100 characters")
    String highestEducation,

    CandidateStatus status,

    @Size(max = 50, message = "Source must not exceed 50 characters")
    String source,

    @Size(max = 500, message = "Resume URL must not exceed 500 characters")
    String resumeUrl,

    String notes,

    @Size(max = 30, message = "A candidate cannot have more than 30 skills")
    Set<@NotBlank @Size(max = 50) String> skills
) {}