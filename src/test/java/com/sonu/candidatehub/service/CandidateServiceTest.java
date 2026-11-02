package com.sonu.candidatehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

import com.sonu.candidatehub.domain.Candidate;
import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateRequest;
import com.sonu.candidatehub.dto.CandidateResponse;
import com.sonu.candidatehub.exception.CandidateNotFoundException;
import com.sonu.candidatehub.exception.DuplicateEmailException;
import com.sonu.candidatehub.mapper.CandidateMapper;
import com.sonu.candidatehub.repository.CandidateRepository;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateMapper candidateMapper;

    @InjectMocks
    private CandidateService candidateService;

    private CandidateRequest validRequest;
    private Candidate savedEntity;

    @BeforeEach
    void setUp() {
        validRequest = new CandidateRequest(
            "Priya", "Sharma", "priya.sharma@example.com", "+91-9876543210",
            "Hyderabad", "TechCorp", "Senior Java Developer",
            "Backend engineer", 5, 30, "B.Tech",
            CandidateStatus.NEW, "LinkedIn", null, null,
            Set.of("java", "spring boot")
        );

        savedEntity = new Candidate();
        savedEntity.setId(1L);
        savedEntity.setFirstName("Priya");
        savedEntity.setEmail("priya.sharma@example.com");
    }

    @Test
    void create_savesAndReturnsResponse_whenEmailIsUnique() {
        when(candidateRepository.existsByEmailIgnoreCase("priya.sharma@example.com")).thenReturn(false);
        when(candidateMapper.toEntity(validRequest)).thenReturn(savedEntity);
        when(candidateRepository.saveAndFlush(savedEntity)).thenReturn(savedEntity);
        when(candidateMapper.toResponse(savedEntity)).thenReturn(
            new CandidateResponse(1L, "Priya", "Sharma", "priya.sharma@example.com",
                null, null, null, null, null, null, null, null,
                CandidateStatus.NEW, null, null, null, Set.of(), null, null)
        );

        CandidateResponse result = candidateService.create(validRequest);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("Priya");
        verify(candidateRepository).saveAndFlush(savedEntity);
    }

    @Test
    void create_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        when(candidateRepository.existsByEmailIgnoreCase("priya.sharma@example.com")).thenReturn(true);

        assertThatThrownBy(() -> candidateService.create(validRequest))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("priya.sharma@example.com");

        verify(candidateRepository, never()).saveAndFlush(any());
    }
    
    @Test
    void create_propagatesOriginalException_whenOtherDatabaseConstraintFails() {

        when(candidateRepository.existsByEmailIgnoreCase("priya.sharma@example.com"))
                .thenReturn(false);

        when(candidateMapper.toEntity(validRequest)).thenReturn(savedEntity);

        when(candidateRepository.saveAndFlush(savedEntity))
                .thenThrow(new DataIntegrityViolationException("Other constraint failed"));

        assertThatThrownBy(() -> candidateService.create(validRequest))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Other constraint failed");
    }

    @Test
    void getById_returnsResponse_whenCandidateExists() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
        when(candidateMapper.toResponse(savedEntity)).thenReturn(
            new CandidateResponse(1L, "Priya", "Sharma", "priya.sharma@example.com",
                null, null, null, null, null, null, null, null,
                CandidateStatus.NEW, null, null, null, Set.of(), null, null)
        );

        CandidateResponse result = candidateService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void getById_throwsNotFoundException_whenCandidateDoesNotExist() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.getById(99L))
            .isInstanceOf(CandidateNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void delete_removesCandidate_whenItExists() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(savedEntity));

        candidateService.delete(1L);

        verify(candidateRepository).delete(savedEntity);
    }

    @Test
    void delete_throwsNotFoundException_whenCandidateDoesNotExist() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.delete(99L))
            .isInstanceOf(CandidateNotFoundException.class);

        verify((CrudRepository<Candidate, Long>) candidateRepository, never()).delete(any());
    }

    @Test
    void update_throwsDuplicateEmailException_whenNewEmailBelongsToAnotherCandidate() {
        CandidateRequest updateRequest = new CandidateRequest(
            "Priya", "Sharma", "taken@example.com", null, null, null, null,
            null, null, null, null, CandidateStatus.SCREENING, null, null, null, null
        );

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
        when(candidateRepository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> candidateService.update(1L, updateRequest))
            .isInstanceOf(DuplicateEmailException.class);

        verify(candidateRepository, never()).saveAndFlush(any());
    }
}