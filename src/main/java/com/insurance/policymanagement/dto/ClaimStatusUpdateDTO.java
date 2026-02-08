package com.insurance.policymanagement.dto;

import com.insurance.policymanagement.model.Claim;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimStatusUpdateDTO {
    
    @NotNull(message = "Status is required")
    private Claim.ClaimStatus status;
    
    private String rejectionReason;
}
