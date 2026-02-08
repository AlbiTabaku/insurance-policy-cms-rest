package com.insurance.policymanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "policy_number", unique = true, nullable = false, length = 20)
    private String policyNumber;
    
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;
    
    @NotNull(message = "Policy type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 20)
    private PolicyType policyType;
    
    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be positive")
    @Column(name = "coverage_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;
    
    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "0.01", message = "Premium amount must be positive")
    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum PolicyType {
        HEALTH, AUTO, HOME, LIFE
    }
    
    public enum PolicyStatus {
        ACTIVE, EXPIRED, CANCELLED
    }
}
