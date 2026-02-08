package com.insurance.policymanagement.service;

import com.insurance.policymanagement.dto.ClaimRequestDTO;
import com.insurance.policymanagement.dto.ClaimResponseDTO;
import com.insurance.policymanagement.dto.ClaimStatusUpdateDTO;
import com.insurance.policymanagement.exception.BusinessRuleException;
import com.insurance.policymanagement.exception.ResourceNotFoundException;
import com.insurance.policymanagement.model.Claim;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.repository.ClaimRepository;
import com.insurance.policymanagement.repository.PolicyRepository;
import com.insurance.policymanagement.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    @Transactional
    public ClaimResponseDTO submitClaim(ClaimRequestDTO requestDTO) {
        log.info("Submitting new claim for policy id: {}", requestDTO.getPolicyId());
        
        // get policy record
        Policy policy = policyRepository.findById(requestDTO.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", requestDTO.getPolicyId()));
        
        // Validate claim
        validateClaimSubmission(requestDTO, policy);
        
        // generate uniq nr
        String claimNumber = generateUniqueClaimNumber();

        Claim claim = Claim.builder()
                .claimNumber(claimNumber)
                .policy(policy)
                .description(requestDTO.getDescription())
                .claimAmount(requestDTO.getClaimAmount())
                .incidentDate(requestDTO.getIncidentDate())
                .status(Claim.ClaimStatus.SUBMITTED)
                .build();
        
        Claim savedClaim = claimRepository.save(claim);
        return ClaimResponseDTO.fromEntity(savedClaim);
    }

    // Method to get a Claim
    @Transactional(readOnly = true)
    public ClaimResponseDTO getClaimById(Long id) {
        log.info("Retrieving claim with id: {}", id);

        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));
        
        return ClaimResponseDTO.fromEntity(claim);
    }


    @Transactional(readOnly = true)
    public List<ClaimResponseDTO> getClaimsByPolicyId(Long policyId) {
        log.info("Retrieving all claims for policy id: {}", policyId);
        
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Policy", "id", policyId);
        }
        
        return claimRepository.findByPolicyId(policyId).stream()
                .map(ClaimResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimResponseDTO updateClaimStatus(Long id, ClaimStatusUpdateDTO statusUpdateDTO) {
        log.info("Updating claim status for id: {} to {}", id, statusUpdateDTO.getStatus());
        
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));
        
        validateStatusTransition(claim, statusUpdateDTO);
        
        claim.setStatus(statusUpdateDTO.getStatus());
        
        if (statusUpdateDTO.getStatus() == Claim.ClaimStatus.REJECTED) {
            if (statusUpdateDTO.getRejectionReason() == null || statusUpdateDTO.getRejectionReason().isBlank()) {
                throw new BusinessRuleException("Rejection reason is required when rejecting a claim");
            }
            claim.setRejectionReason(statusUpdateDTO.getRejectionReason());
        }
        
        Claim updatedClaim = claimRepository.save(claim);
        log.info("Claim status updated successfully: {}", claim.getClaimNumber());
        
        return ClaimResponseDTO.fromEntity(updatedClaim);
    }
    
    private void validateClaimSubmission(ClaimRequestDTO requestDTO, Policy policy) {
        // Check if policy is active
        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE) {
            throw new BusinessRuleException("Claims can only be submitted for ACTIVE policies");
        }
        
        // check if claim amount pass coverage
        if (requestDTO.getClaimAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new BusinessRuleException("Claim amount cannot exceed policy coverage amount");
        }
        
        // check if incident date is within policy period
        if (requestDTO.getIncidentDate().isBefore(policy.getStartDate()) || 
            requestDTO.getIncidentDate().isAfter(policy.getEndDate())) {
            throw new BusinessRuleException("Incident date must be within the policy active period");
        }
    }
    
    private void validateStatusTransition(Claim claim, ClaimStatusUpdateDTO statusUpdateDTO) {
        // check if claim is already approved or rejected
        if (claim.getStatus() == Claim.ClaimStatus.APPROVED || 
            claim.getStatus() == Claim.ClaimStatus.REJECTED) {
            throw new BusinessRuleException("Cannot change status of an already approved or rejected claim");
        }
        
        // check valid status transitions
        if (claim.getStatus() == Claim.ClaimStatus.SUBMITTED) {
            if (statusUpdateDTO.getStatus() != Claim.ClaimStatus.APPROVED && 
                statusUpdateDTO.getStatus() != Claim.ClaimStatus.REJECTED) {
                throw new BusinessRuleException("Status can only transition from SUBMITTED to APPROVED or REJECTED");
            }
        }
    }

    private String generateUniqueClaimNumber() {
        String claimNumber;
        do {
            claimNumber = NumberGenerator.generateClaimNumber();
        } while (claimRepository.existsByClaimNumber(claimNumber));
        
        return claimNumber;
    }
}
