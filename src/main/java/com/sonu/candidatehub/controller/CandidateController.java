package com.sonu.candidatehub.controller;

import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateFilter;
import com.sonu.candidatehub.dto.CandidateRequest;
import com.sonu.candidatehub.dto.CandidateResponse;
import com.sonu.candidatehub.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private static final int MAX_PAGE_SIZE = 100;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sortParam) {
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}