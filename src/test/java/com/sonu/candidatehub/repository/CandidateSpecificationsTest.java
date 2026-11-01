package com.sonu.candidatehub.repository;

import com.sonu.candidatehub.domain.Candidate;
import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CandidateSpecificationsTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @BeforeEach
    void setUp() {
        candidateRepository.deleteAll();

        save("Priya", "Sharma", "priya@example.com", "Hyderabad",
            CandidateStatus.NEW, 5, Set.of("java", "spring boot"));

        save("Arjun", "Mehta", "arjun@example.com", "Bangalore",
            CandidateStatus.SCREENING, 3, Set.of("java", "kafka"));

        save("Kavya", "Reddy", "kavya@example.com", "Hyderabad",
            CandidateStatus.INTERVIEWING, 8, Set.of("python", "django"));

        save("Rahul", "Verma", "rahul@example.com", "Mumbai",
            CandidateStatus.NEW, 1, Set.of("java"));
    }

    private void save(String first, String last, String email, String location,
                       CandidateStatus status, int experience, Set<String> skills) {
        Candidate c = new Candidate();
        c.setFirstName(first);
        c.setLastName(last);
        c.setEmail(email);
        c.setLocation(location);
        c.setStatus(status);
        c.setExperienceYears(experience);
        c.setSkills(skills);
        candidateRepository.save(c);
    }

    private CandidateFilter emptyFilter() {
        return new CandidateFilter(null, null, false, null, null, null, null, null, null, null);
    }

    @Test
    void filtersByLocation() {
        CandidateFilter filter = new CandidateFilter(
            null, null, false, "Hyderabad", null, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(2)
            .extracting(Candidate::getFirstName)
            .containsExactlyInAnyOrder("Priya", "Kavya");
    }

    @Test
    void filtersByStatus() {
        CandidateFilter filter = new CandidateFilter(
            null, null, false, null, CandidateStatus.NEW, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(2)
            .extracting(Candidate::getFirstName)
            .containsExactlyInAnyOrder("Priya", "Rahul");
    }

    @Test
    void filtersByExperienceRange() {
        CandidateFilter filter = new CandidateFilter(
            null, null, false, null, null, 3, 6, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(2)
            .extracting(Candidate::getFirstName)
            .containsExactlyInAnyOrder("Priya", "Arjun");
    }

    @Test
    void freeTextSearch_matchesFirstName() {
        CandidateFilter filter = new CandidateFilter(
            "arju", null, false, null, null, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Arjun");
    }

    @Test
    void skillFilter_matchAny_returnsCandidatesWithAtLeastOneSkill() {
        CandidateFilter filter = new CandidateFilter(
            null, Set.of("python", "kafka"), false, null, null, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(2)
            .extracting(Candidate::getFirstName)
            .containsExactlyInAnyOrder("Arjun", "Kavya");
    }

    @Test
    void skillFilter_matchAll_returnsOnlyCandidatesWithEverySkill() {
        CandidateFilter filter = new CandidateFilter(
            null, Set.of("java", "kafka"), true, null, null, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Arjun");
    }

    @Test
    void combinedFilters_locationAndStatus_narrowsCorrectly() {
        CandidateFilter filter = new CandidateFilter(
            null, null, false, "Hyderabad", CandidateStatus.NEW, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Priya");
    }

    @Test
    void emptyFilter_returnsAllCandidates() {
        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(emptyFilter()));

        assertThat(results).hasSize(4);
    }

    @Test
    void likeSearch_withWildcardCharacters_isTreatedLiterally() {
        save("Test", "Percent", "percent@example.com", "Remote",
            CandidateStatus.NEW, 2, Set.of());

        Candidate percentCandidate = candidateRepository.findAll().stream()
            .filter(c -> c.getFirstName().equals("Test"))
            .findFirst().orElseThrow();
        percentCandidate.setHeadline("Open to 50% remote roles");
        candidateRepository.save(percentCandidate);

        CandidateFilter filter = new CandidateFilter(
            "50%", null, false, null, null, null, null, null, null, null
        );

        List<Candidate> results = candidateRepository.findAll(CandidateSpecifications.from(filter));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Test");
    }
}