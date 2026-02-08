package com.insurance.policymanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimRequestDTO {
    
    @NotNull(message = "Policy ID is required")
    private Long policyId;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be positive")
    private BigDecimal claimAmount;
    
    @NotNull(message = "date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate;
}
