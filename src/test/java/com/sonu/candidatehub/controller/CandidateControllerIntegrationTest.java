package com.sonu.candidatehub.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sonu.candidatehub.domain.CandidateStatus;
import com.sonu.candidatehub.dto.CandidateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private CandidateRequest sampleRequest(String email) {
        return new CandidateRequest(
            "Arjun", "Mehta", email, "+91-9988776655",
            "Bangalore", "DataWorks", "Backend Engineer",
            "Java backend developer", 4, 15, "B.Tech",
            CandidateStatus.NEW, "Referral", null, null,
            Set.of("java", "kafka")
        );
    }

    @Test
    void create_returns201AndLocationHeader_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest("arjun1@example.com"))))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.firstName").value("Arjun"))
            .andExpect(jsonPath("$.email").value("arjun1@example.com"))
            .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void create_returns400_whenEmailIsInvalid() throws Exception {
        CandidateRequest invalid = new CandidateRequest(
            "Arjun", "Mehta", "not-an-email", null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void create_returns400_whenFirstNameIsBlank() throws Exception {
        CandidateRequest invalid = new CandidateRequest(
            "", "Mehta", "valid@example.com", null, null, null, null,
            null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.firstName").exists());
    }

    @Test
    void create_returns409_whenEmailAlreadyExists() throws Exception {
        String email = "duplicate@example.com";

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest(email))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest(email))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail", containsString(email)));
    }

    @Test
    void getById_returns404_whenCandidateDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/candidates/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns200_whenCandidateExists() throws Exception {
        String response = mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest("findme@example.com"))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/v1/candidates/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("findme@example.com"));
    }

    @Test
    void search_returnsPagedResults_withFilters() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest("searchtest@example.com"))));

        mockMvc.perform(get("/api/v1/candidates")
                .param("location", "Bangalore")
                .param("minExperience", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void search_enforcesMaxPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/candidates").param("size", "9999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageable.pageSize").value(100));
    }

    @Test
    void update_returns404_whenCandidateDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/v1/candidates/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest("ghost@example.com"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204_thenGetReturns404() throws Exception {
        String response = mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest("deleteme@example.com"))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/v1/candidates/" + id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/candidates/" + id))
            .andExpect(status().isNotFound());
    }
}