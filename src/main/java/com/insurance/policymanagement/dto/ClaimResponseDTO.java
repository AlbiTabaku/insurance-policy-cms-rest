package com.insurance.policymanagement.dto;

import com.insurance.policymanagement.model.Claim;
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
public class ClaimResponseDTO {
    
    private Long id;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private String description;
    private BigDecimal claimAmount;
    private LocalDate incidentDate;
    private Claim.ClaimStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ClaimResponseDTO fromEntity(Claim claim) {
        return ClaimResponseDTO.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicy().getId())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .description(claim.getDescription())
                .claimAmount(claim.getClaimAmount())
                .incidentDate(claim.getIncidentDate())
                .status(claim.getStatus())
                .rejectionReason(claim.getRejectionReason())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
