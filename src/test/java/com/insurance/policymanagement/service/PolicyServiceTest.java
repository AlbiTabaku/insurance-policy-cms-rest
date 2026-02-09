package com.insurance.policymanagement.service;

import com.insurance.policymanagement.dto.PagedResponse;
import com.insurance.policymanagement.dto.PolicyRequestDTO;
import com.insurance.policymanagement.dto.PolicyResponseDTO;
import com.insurance.policymanagement.exception.BusinessRuleException;
import com.insurance.policymanagement.exception.ResourceNotFoundException;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyService policyService;

    private PolicyRequestDTO validPolicyRequest;
    private Policy samplePolicy;

    @BeforeEach
    void setUp() {
        validPolicyRequest = PolicyRequestDTO.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        samplePolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-100001")
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreatePolicy_Success() {
        // Arrange
        when(policyRepository.save(any(Policy.class))).thenReturn(samplePolicy);

        // Act
        PolicyResponseDTO result = policyService.createPolicy(validPolicyRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john.doe@email.com", result.getCustomerEmail());
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testCreatePolicy_EndDateLessThan6Months_ThrowsException() {
        // Arrange
        validPolicyRequest.setEndDate(LocalDate.now().plusMonths(3));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> policyService.createPolicy(validPolicyRequest)
        );

        assertTrue(exception.getMessage().contains("6 months"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testCreatePolicy_CoverageNotGreaterThanPremium_ThrowsException() {
        // Arrange
        validPolicyRequest.setPremiumAmount(new BigDecimal("100000.00"));
        validPolicyRequest.setCoverageAmount(new BigDecimal("50000.00"));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> policyService.createPolicy(validPolicyRequest)
        );

        assertTrue(exception.getMessage().contains("Coverage amount must be greater than premium amount"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testGetPolicyById_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(samplePolicy));

        // Act
        PolicyResponseDTO result = policyService.getPolicyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("POL-2024-100001", result.getPolicyNumber());
    }

    @Test
    void testGetPolicyById_NotFound_ThrowsException() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> policyService.getPolicyById(999L));
    }

    @Test
    void testGetAllPolicies_WithPagination() {
        // Arrange
        List<Policy> policies = Arrays.asList(samplePolicy, samplePolicy);
        Page<Policy> policyPage = new PageImpl<>(policies, PageRequest.of(0, 20), 2);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                null, null, null, null, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
    }

    @Test
    void testGetAllPolicies_WithAllFilters() {
        // Arrange
        Policy healthPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-100001")
                .customerName("John Doe")
                .customerEmail("john.doe@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(Policy.PolicyStatus.ACTIVE)
                .build();

        Page<Policy> policyPage = new PageImpl<>(Arrays.asList(healthPolicy), PageRequest.of(0, 20), 1);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                "john.doe@email.com",
                "POL-2024",
                Policy.PolicyStatus.ACTIVE,
                Policy.PolicyType.HEALTH,
                PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("john.doe@email.com", result.getContent().get(0).getCustomerEmail());
        assertEquals(Policy.PolicyType.HEALTH, result.getContent().get(0).getPolicyType());
        assertEquals(Policy.PolicyStatus.ACTIVE, result.getContent().get(0).getStatus());
    }

    @Test
    void testGetAllPolicies_ByCustomerEmail() {
        // Arrange
        Page<Policy> policyPage = new PageImpl<>(Arrays.asList(samplePolicy), PageRequest.of(0, 20), 1);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                "john.doe@email.com", null, null, null, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("john.doe@email.com", result.getContent().get(0).getCustomerEmail());
    }

    @Test
    void testGetAllPolicies_ByStatus() {
        // Arrange
        Page<Policy> policyPage = new PageImpl<>(Arrays.asList(samplePolicy), PageRequest.of(0, 20), 1);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                null, null, Policy.PolicyStatus.ACTIVE, null, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Policy.PolicyStatus.ACTIVE, result.getContent().get(0).getStatus());
    }

    @Test
    void testGetAllPolicies_ByPolicyType() {
        // Arrange
        Page<Policy> policyPage = new PageImpl<>(Arrays.asList(samplePolicy), PageRequest.of(0, 20), 1);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                null, null, null, Policy.PolicyType.HEALTH, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Policy.PolicyType.HEALTH, result.getContent().get(0).getPolicyType());
    }

    @Test
    void testGetAllPolicies_ByPolicyNumber() {
        // Arrange
        Page<Policy> policyPage = new PageImpl<>(Arrays.asList(samplePolicy), PageRequest.of(0, 20), 1);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                null, "POL-2024", null, null, PageRequest.of(0, 20));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getPolicyNumber().contains("POL-2024"));
    }

    @Test
    void testGetAllPolicies_VerifyPaginationMetadata() {
        // Arrange
        List<Policy> policies = Arrays.asList(samplePolicy, samplePolicy, samplePolicy);
        Page<Policy> policyPage = new PageImpl<>(policies.subList(0, 2), PageRequest.of(0, 2), 3);
        when(policyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(policyPage);

        // Act
        PagedResponse<PolicyResponseDTO> result = policyService.getAllPolicies(
                null, null, null, null, PageRequest.of(0, 2));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
        assertFalse(result.isEmpty());
    }


    @Test
    void testRenewPolicy_CancelledPolicy_ThrowsException() {
        // Arrange
        samplePolicy.setStatus(Policy.PolicyStatus.CANCELLED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(samplePolicy));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> policyService.renewPolicy(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot renew a cancelled policy"));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void testCancelPolicy_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(samplePolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(samplePolicy);

        // Act
        policyService.cancelPolicy(1L);

        // Assert
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testCancelPolicy_NotActive_ThrowsException() {
        // Arrange
        samplePolicy.setStatus(Policy.PolicyStatus.EXPIRED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(samplePolicy));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> policyService.cancelPolicy(1L)
        );

        assertTrue(exception.getMessage().contains("Only ACTIVE policies can be cancelled"));
    }
}
