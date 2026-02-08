package com.insurance.policymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.policymanagement.dto.PolicyRequestDTO;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PolicyRepository policyRepository;

    private PolicyRequestDTO validPolicyRequest;

    @BeforeEach
    void setUp() {
        policyRepository.deleteAll();

        validPolicyRequest = PolicyRequestDTO.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();
    }

    @Test
    void testCreatePolicy_Success() throws Exception {
        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPolicyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyNumber", notNullValue()))
                .andExpect(jsonPath("$.customerName", is("John Doe")))
                .andExpect(jsonPath("$.customerEmail", is("john.doe@email.com")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void testCreatePolicy_InvalidEmail_ReturnsBadRequest() throws Exception {
        validPolicyRequest.setCustomerEmail("invalid-email");

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPolicyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    void testGetPolicyById_Success() throws Exception {
        // Create a policy first
        Policy policy = policyRepository.save(Policy.builder()
                .policyNumber("POL-2024-TEST01")
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/policies/{id}", policy.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(policy.getId().intValue())))
                .andExpect(jsonPath("$.policyNumber", is("POL-2024-TEST01")));
    }

    @Test
    void testGetPolicyById_NotFound() throws Exception {
        mockMvc.perform(get("/api/policies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllPolicies_DefaultPagination() throws Exception {
        // Create test policies
        policyRepository.save(Policy.builder()
                .policyNumber("POL-2024-TEST01")
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/policies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.empty", is(false)));
    }

    @Test
    void testGetAllPolicies_WithFilters() throws Exception {
        // Create test policies with different attributes
        policyRepository.save(Policy.builder()
                .policyNumber("POL-2024-HEALTH01")
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.ACTIVE)
                .build());

        policyRepository.save(Policy.builder()
                .policyNumber("POL-2024-AUTO01")
                .customerName("Jane Smith")
                .customerEmail("jane.smith@email.com")
                .policyType(Policy.PolicyType.AUTO)
                .coverageAmount(new BigDecimal("50000.00"))
                .premiumAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.EXPIRED)
                .build());

        // Test filter by customer email
        mockMvc.perform(get("/api/policies")
                        .param("customerEmail", "john.doe@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerEmail", is("john.doe@email.com")));

        // Test filter by policy type
        mockMvc.perform(get("/api/policies")
                        .param("policyType", "HEALTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].policyType", everyItem(is("HEALTH"))));

        // Test filter by status
        mockMvc.perform(get("/api/policies")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("ACTIVE"))));

        // Test filter by policy number (partial match)
        mockMvc.perform(get("/api/policies")
                        .param("policyNumber", "HEALTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].policyNumber", containsString("HEALTH")));

        // Test combined filters
        mockMvc.perform(get("/api/policies")
                        .param("status", "ACTIVE")
                        .param("policyType", "HEALTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("ACTIVE"))))
                .andExpect(jsonPath("$.content[*].policyType", everyItem(is("HEALTH"))));
    }

    @Test
    void testGetAllPolicies_WithPagination() throws Exception {
        // Create multiple test policies
        for (int i = 1; i <= 5; i++) {
            policyRepository.save(Policy.builder()
                    .policyNumber("POL-2024-TEST" + String.format("%02d", i))
                    .customerName("Customer " + i)
                    .customerEmail("customer" + i + "@email.com")
                    .policyType(Policy.PolicyType.HEALTH)
                    .coverageAmount(new BigDecimal("100000.00"))
                    .premiumAmount(new BigDecimal("5000.00"))
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .status(Policy.PolicyStatus.ACTIVE)
                    .build());
        }

        // Test first page
        mockMvc.perform(get("/api/policies")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));

        // Test second page
        mockMvc.perform(get("/api/policies")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(false)));

        // Test last page
        mockMvc.perform(get("/api/policies")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(true)));
    }
}
