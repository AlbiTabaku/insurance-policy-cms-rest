package com.insurance.policymanagement.service;

import com.insurance.policymanagement.dto.PagedResponse;
import com.insurance.policymanagement.dto.PolicyRequestDTO;
import com.insurance.policymanagement.dto.PolicyResponseDTO;
import com.insurance.policymanagement.exception.BusinessRuleException;
import com.insurance.policymanagement.exception.ResourceNotFoundException;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.repository.PolicyRepository;
import com.insurance.policymanagement.repository.PolicySpecifications;
import com.insurance.policymanagement.util.NumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;

    @Transactional
    public PolicyResponseDTO createPolicy(PolicyRequestDTO requestDTO) {
        log.info("Creating new policy for customer: {}", requestDTO.getCustomerEmail());

        validatePolicyRequest(requestDTO);

        // generate uniq policy number
        String policyNumber = generateUniquePolicyNumber();

        // Create policy record
        Policy policy = Policy.builder()
                .policyNumber(policyNumber)
                .customerName(requestDTO.getCustomerName())
                .customerEmail(requestDTO.getCustomerEmail())
                .policyType(requestDTO.getPolicyType())
                .coverageAmount(requestDTO.getCoverageAmount())
                .premiumAmount(requestDTO.getPremiumAmount())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .status(Policy.PolicyStatus.ACTIVE)
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy created successfully with number: {}", policyNumber);

        return PolicyResponseDTO.fromEntity(savedPolicy);
    }

    @Transactional(readOnly = true)
    public PolicyResponseDTO getPolicyById(Long id) {
        log.info("Retrieving policy with id: {}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));

        return PolicyResponseDTO.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PolicyResponseDTO> getAllPolicies(
            String customerEmail,
            String policyNumber,
            Policy.PolicyStatus status,
            Policy.PolicyType policyType,
            Pageable pageable) {

        log.info("Retrieving policies with filters - customerEmail: {}, policyNumber: {}, status: {}, policyType: {}, page: {}, size: {}",
                customerEmail, policyNumber, status, policyType, pageable.getPageNumber(), pageable.getPageSize());

        // create dynamic specification for search based on params
        Specification<Policy> spec = Specification.where(PolicySpecifications.hasCustomerEmail(customerEmail))
                .and(PolicySpecifications.hasPolicyNumberContaining(policyNumber))
                .and(PolicySpecifications.hasStatus(status))
                .and(PolicySpecifications.hasPolicyType(policyType));

        // exec query with pagination
        Page<Policy> policyPage = policyRepository.findAll(spec, pageable);

        // Map to dto each object
        Page<PolicyResponseDTO> responsePage = policyPage.map(PolicyResponseDTO::fromEntity);

        log.info("Retrieved {} policies out of {} total", responsePage.getNumberOfElements(), responsePage.getTotalElements());

        return PagedResponse.fromPage(responsePage);
    }

    @Transactional
    public PolicyResponseDTO renewPolicy(Long id) {
        log.info("Renewing policy with id: {}", id);

        Policy existingPolicy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));

        if (existingPolicy.getStatus() == Policy.PolicyStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot renew a cancelled policy");
        }

        // create new policy with updated dates: 1 year from original end date
        LocalDate newStartDate = existingPolicy.getEndDate().plusDays(1);
        LocalDate newEndDate = newStartDate.plusYears(1);

        String newPolicyNumber = generateUniquePolicyNumber();

        Policy renewedPolicy = Policy.builder()
                .policyNumber(newPolicyNumber)
                .customerName(existingPolicy.getCustomerName())
                .customerEmail(existingPolicy.getCustomerEmail())
                .policyType(existingPolicy.getPolicyType())
                .coverageAmount(existingPolicy.getCoverageAmount())
                .premiumAmount(existingPolicy.getPremiumAmount())
                .startDate(newStartDate)
                .endDate(newEndDate)
                .status(Policy.PolicyStatus.ACTIVE)
                .build();

        Policy savedPolicy = policyRepository.save(renewedPolicy);
        return PolicyResponseDTO.fromEntity(savedPolicy);
    }

    @Transactional
    public void cancelPolicy(Long id) {
        log.info("Cancelling policy with id: {}", id);

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));

        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE policies can be cancelled");
        }

        policy.setStatus(Policy.PolicyStatus.CANCELLED);
        policyRepository.save(policy);

        log.info("Policy cancelled successfully: {}", policy.getPolicyNumber());
    }

    private void validatePolicyRequest(PolicyRequestDTO requestDTO) {
        // check if end date is at least 6 months after start date
        long monthsBetween = ChronoUnit.MONTHS.between(requestDTO.getStartDate(), requestDTO.getEndDate());
        if (monthsBetween < 6) {
            throw new BusinessRuleException("End date must be at least 6 months after start date");
        }

        // check if coverage amount is greater than premium amount
        if (requestDTO.getCoverageAmount().compareTo(requestDTO.getPremiumAmount()) <= 0) {
            throw new BusinessRuleException("Coverage amount must be greater than premium amount");
        }
    }

    private String generateUniquePolicyNumber() {
        String policyNumber;
        do {
            policyNumber = NumberGenerator.generatePolicyNumber();
        } while (policyRepository.existsByPolicyNumber(policyNumber));

        return policyNumber;
    }
}