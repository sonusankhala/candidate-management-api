package com.sonu.candidatehub.controller;

import java.net.URI;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateFilter;
import com.sonu.candidatehub.dto.CandidateRequest;
import com.sonu.candidatehub.dto.CandidateResponse;
import com.sonu.candidatehub.service.CandidateService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Validated
public class CandidateController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
    	    "id",
    	    "firstName",
    	    "lastName",
    	    "email",
    	    "location",
    	    "experienceYears",
    	    "createdAt",
    	    "updatedAt"
    	);

    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
        CandidateResponse created = candidateService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/candidates/" + created.id()))
            .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CandidateResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Set<String> skills,
            @RequestParam(required = false, defaultValue = "false") boolean matchAllSkills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) CandidateStatus status,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) String currentCompany,
            @RequestParam(required = false) Integer maxNoticePeriod,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, parseSort(sort));

        CandidateFilter filter = new CandidateFilter(
            q, skills, matchAllSkills, location, status,
            minExperience, maxExperience, currentCompany, maxNoticePeriod, source
        );

        return ResponseEntity.ok(candidateService.search(filter, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request
    ) {
        return ResponseEntity.ok(candidateService.update(id, request));
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<CandidateResponse> patch(
            @PathVariable Long id,
            @RequestBody CandidateRequest request
    ) {
        return ResponseEntity.ok(candidateService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
	private Sort parseSort(String sortParam) {

		String[] parts = sortParam.split(",", -1);

		String property = parts[0].trim();

		if (!ALLOWED_SORT_FIELDS.contains(property)) {
			throw new IllegalArgumentException("Invalid sort field: " + property);
		}

		if (parts.length > 2) {
			throw new IllegalArgumentException("Invalid sort parameter");
		}

		Sort.Direction direction = Sort.Direction.DESC;

		if (parts.length == 2) {
			String directionParam = parts[1].trim();

			if (directionParam.equalsIgnoreCase("asc")) {
				direction = Sort.Direction.ASC;
			} else if (directionParam.equalsIgnoreCase("desc")) {
				direction = Sort.Direction.DESC;
			} else {
				throw new IllegalArgumentException("Invalid sort direction: " + directionParam);
			}
		}

		return Sort.by(direction, property);
	}

}