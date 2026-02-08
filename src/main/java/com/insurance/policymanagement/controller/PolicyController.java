package com.insurance.policymanagement.controller;

import com.insurance.policymanagement.dto.PagedResponse;
import com.insurance.policymanagement.dto.PolicyRequestDTO;
import com.insurance.policymanagement.dto.PolicyResponseDTO;
import com.insurance.policymanagement.model.Policy;
import com.insurance.policymanagement.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Policy Management", description = "APIs for managing insurance policies")
public class PolicyController {

    private final PolicyService policyService;

    // Method to create a  policy
    @PostMapping
    @Operation(summary = "Create a new policy", description = "Creates a new insurance policy with customer information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<PolicyResponseDTO> createPolicy(@Valid @RequestBody PolicyRequestDTO requestDTO) {
        PolicyResponseDTO response = policyService.createPolicy(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Method to get the policy
    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID", description = "Retrieves a specific policy by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy found"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<PolicyResponseDTO> getPolicyById(@PathVariable Long id) {
        PolicyResponseDTO response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    // Method to get all the policies
    @GetMapping
    @Operation(summary = "Get all policies with pagination and filtering",
            description = "Retrieves insurance policies with optional pagination and filtering by customer email, policy number, status, and type etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policies retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<PagedResponse<PolicyResponseDTO>> getAllPolicies(
            @Parameter(description = "Customer email (exact match)")
            @RequestParam(required = false) String customerEmail,

            @Parameter(description = "Policy number (partial match)")
            @RequestParam(required = false) String policyNumber,

            @Parameter(description = "Policy status (ACTIVE, EXPIRED, CANCELLED)")
            @RequestParam(required = false) Policy.PolicyStatus status,

            @Parameter(description = "Policy type (HEALTH, AUTO, HOME, LIFE)")
            @RequestParam(required = false) Policy.PolicyType policyType,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        // Create pageable with default sort by createdAt descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        PagedResponse<PolicyResponseDTO> response = policyService.getAllPolicies(
                customerEmail, policyNumber, status, policyType, pageable);

        return ResponseEntity.ok(response);
    }

    // Method for renewing a policy
    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew a policy", description = "Creates a new policy by renewing an existing one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Policy renewed successfully"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "409", description = "Policy cannot be renewed")
    })
    public ResponseEntity<PolicyResponseDTO> renewPolicy(@PathVariable Long id) {
        PolicyResponseDTO response = policyService.renewPolicy(id);
        return ResponseEntity.ok(response);
    }

    // Method for deleting a policy
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a policy", description = "Cancels an active insurance policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Policy cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "409", description = "Policy cannot be cancelled")
    })
    public ResponseEntity<Void> cancelPolicy(@PathVariable Long id) {
        policyService.cancelPolicy(id);
        return ResponseEntity.noContent().build();
    }
}