package com.sonu.candidatehub.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sonu.candidatehub.domain.Candidate;
import com.sonu.candidatehub.dto.CandidateFilter;
import com.sonu.candidatehub.dto.CandidateRequest;
import com.sonu.candidatehub.dto.CandidateResponse;
import com.sonu.candidatehub.exception.CandidateNotFoundException;
import com.sonu.candidatehub.exception.DuplicateEmailException;
import com.sonu.candidatehub.mapper.CandidateMapper;
import com.sonu.candidatehub.repository.CandidateRepository;
import com.sonu.candidatehub.repository.CandidateSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateMapper candidateMapper;

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (candidateRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        Candidate candidate = candidateMapper.toEntity(request);

        try {
            candidate = candidateRepository.saveAndFlush(candidate);
        } catch (DataIntegrityViolationException e) {
            // Backup for the race condition: two concurrent requests both
            // passed the existsBy check above before either one committed.
        	if (isDuplicateEmailViolation(e)) {
                throw new DuplicateEmailException(normalizedEmail);
            }
            throw e;
        }

        log.info("Created candidate id={} email={}", candidate.getId(), normalizedEmail);
        return candidateMapper.toResponse(candidate);
    }

    private boolean isDuplicateEmailViolation(DataIntegrityViolationException ex) {

        Throwable cause = ex;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException violationException) {

                String constraintName = violationException.getConstraintName();

                return "ux_candidates_email".equalsIgnoreCase(constraintName);
            }

            cause = cause.getCause();
        }

        return false;
    }

	@Transactional(readOnly = true)
    public CandidateResponse getById(Long id) {
        Candidate candidate = findCandidateOrThrow(id);
        return candidateMapper.toResponse(candidate);
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponse> search(CandidateFilter filter, Pageable pageable) {
        return candidateRepository
            .findAll(CandidateSpecifications.from(filter), pageable)
            .map(candidateMapper::toResponse);
    }

    @Transactional
    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate candidate = findCandidateOrThrow(id);

        String normalizedEmail = request.email().trim().toLowerCase();
        if (candidateRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        candidateMapper.updateEntity(candidate, request);

        try {
            candidate = candidateRepository.saveAndFlush(candidate);
        } catch (DataIntegrityViolationException e) {
        	if (isDuplicateEmailViolation(e)) {
                throw new DuplicateEmailException(normalizedEmail);
            }
            throw e;
        }

        log.info("Updated candidate id={}", candidate.getId());
        return candidateMapper.toResponse(candidate);
    }
    
    @Transactional
    public CandidateResponse patch(Long id, CandidateRequest request) {
    	Candidate candidate=findCandidateOrThrow(id);
    	if(request.email()!=null) {
    		String normalizedEmail = request.email().trim().toLowerCase();
            if (candidateRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
                throw new DuplicateEmailException(normalizedEmail);
            }
    	}
    	candidateMapper.patchEntity(candidate, request);

        try {
            candidate = candidateRepository.saveAndFlush(candidate);
        } catch (DataIntegrityViolationException e) {
        	if (isDuplicateEmailViolation(e)) {
                throw new DuplicateEmailException(request.email());
            }
            throw e;
        }

        log.info("Patched candidate id={}", candidate.getId());
        return candidateMapper.toResponse(candidate);
    }

    @Transactional
    public void delete(Long id) {
        Candidate candidate = findCandidateOrThrow(id);
        candidateRepository.delete(candidate);
        log.info("Deleted candidate id={}", id);
    }

    private Candidate findCandidateOrThrow(Long id) {
        return candidateRepository.findById(id)
            .orElseThrow(() -> new CandidateNotFoundException(id));
    }
}