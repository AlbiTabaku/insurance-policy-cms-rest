package com.insurance.policymanagement.service;

import com.insurance.policymanagement.dto.ClaimRequestDTO;
import com.insurance.policymanagement.dto.ClaimResponseDTO;
import com.insurance.policymanagement.dto.ClaimStatusUpdateDTO;
import com.insurance.policymanagement.exception.BusinessRuleException;
import com.insurance.policymanagement.model.Claim;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.repository.ClaimRepository;
import com.insurance.policymanagement.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ClaimService claimService;

    private ClaimRequestDTO validClaimRequest;
    private Policy activePolicy;
    private Claim sampleClaim;

    @BeforeEach
    void setUp() {
        activePolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2024-100001")
                .customerName("Albi Tabaku")
                .customerEmail("albi.tabaku@email.com")
                .policyType(Policy.PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("100000.00"))
                .premiumAmount(new BigDecimal("5000.00"))
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .status(Policy.PolicyStatus.ACTIVE)
                .build();

        validClaimRequest = ClaimRequestDTO.builder()
                .policyId(1L)
                .description("Medical treatment")
                .claimAmount(new BigDecimal("10000.00"))
                .incidentDate(LocalDate.now().minusDays(10))
                .build();

        sampleClaim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-2024-200001")
                .policy(activePolicy)
                .description("Medical treatment")
                .claimAmount(new BigDecimal("10000.00"))
                .incidentDate(LocalDate.now().minusDays(10))
                .status(Claim.ClaimStatus.SUBMITTED)
                .build();
    }

    @Test
    void testSubmitClaim_PolicyNotActive_ThrowsException() {
        // Arrange
        activePolicy.setStatus(Policy.PolicyStatus.EXPIRED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> claimService.submitClaim(validClaimRequest)
        );

        assertTrue(exception.getMessage().contains("ACTIVE policies"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void testSubmitClaim_AmountExceedsCoverage_ThrowsException() {
        // Arrange
        validClaimRequest.setClaimAmount(new BigDecimal("150000.00"));
        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> claimService.submitClaim(validClaimRequest)
        );

        assertTrue(exception.getMessage().contains("exceed policy coverage"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void testSubmitClaim_IncidentDateOutsidePolicyPeriod_ThrowsException() {
        // Arrange
        validClaimRequest.setIncidentDate(LocalDate.now().minusYears(2));
        when(policyRepository.findById(1L)).thenReturn(Optional.of(activePolicy));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> claimService.submitClaim(validClaimRequest)
        );

        assertTrue(exception.getMessage().contains("within the policy active period"));
        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    void testGetClaimById_Success() {
        // Arrange
        when(claimRepository.findById(1L)).thenReturn(Optional.of(sampleClaim));

        // Act
        ClaimResponseDTO result = claimService.getClaimById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CLM-2024-200001", result.getClaimNumber());
    }

    @Test
    void testGetClaimsByPolicyId_Success() {
        // Arrange
        when(policyRepository.existsById(1L)).thenReturn(true);
        when(claimRepository.findByPolicyId(1L)).thenReturn(Arrays.asList(sampleClaim));

        // Act
        List<ClaimResponseDTO> result = claimService.getClaimsByPolicyId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateClaimStatus_ApproveSuccess() {
        // Arrange
        ClaimStatusUpdateDTO statusUpdate = ClaimStatusUpdateDTO.builder()
                .status(Claim.ClaimStatus.APPROVED)
                .build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(sampleClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);

        // Act
        ClaimResponseDTO result = claimService.updateClaimStatus(1L, statusUpdate);

        // Assert
        assertNotNull(result);
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void testUpdateClaimStatus_RejectWithoutReason_ThrowsException() {
        // Arrange
        ClaimStatusUpdateDTO statusUpdate = ClaimStatusUpdateDTO.builder()
                .status(Claim.ClaimStatus.REJECTED)
                .build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(sampleClaim));
        // Act & Assert
        BusinessRuleException exception = assertThrows(
        BusinessRuleException.class, () -> claimService.updateClaimStatus(1L, statusUpdate)
        );

        assertTrue(exception.getMessage().contains("Rejection reason is required"));
    }

    @Test
    void testUpdateClaimStatus_AlreadyApproved_ThrowsException() {
        // Arrange
        sampleClaim.setStatus(Claim.ClaimStatus.APPROVED);
        ClaimStatusUpdateDTO statusUpdate = ClaimStatusUpdateDTO.builder()
                .status(Claim.ClaimStatus.REJECTED)
                .rejectionReason("Changed mind")
                .build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(sampleClaim));
        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,() -> claimService.updateClaimStatus(1L, statusUpdate)
        );
        assertTrue(exception.getMessage().contains("already approved or rejected"));
        verify(claimRepository, never()).save(any(Claim.class));
    }
}