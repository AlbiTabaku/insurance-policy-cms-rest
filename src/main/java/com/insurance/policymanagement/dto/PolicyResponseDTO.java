package com.insurance.policymanagement.dto;

import com.insurance.policymanagement.model.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyResponseDTO {
    
    private Long id;
    private String policyNumber;
    private String customerName;
    private String customerEmail;
    private Policy.PolicyType policyType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Policy.PolicyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PolicyResponseDTO fromEntity(Policy policy) {
        return PolicyResponseDTO.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerName(policy.getCustomerName())
                .customerEmail(policy.getCustomerEmail())
                .policyType(policy.getPolicyType())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
