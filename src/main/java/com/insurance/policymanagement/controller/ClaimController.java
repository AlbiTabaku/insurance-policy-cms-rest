package com.insurance.policymanagement.controller;

import com.insurance.policymanagement.dto.ClaimRequestDTO;
import com.insurance.policymanagement.dto.ClaimResponseDTO;
import com.insurance.policymanagement.dto.ClaimStatusUpdateDTO;
import com.insurance.policymanagement.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claim Management", description = "APIs for managing insurance claims")
public class ClaimController {
    
    private final ClaimService claimService;

    // Method to submit a Claim
    @PostMapping
    @Operation(summary = "Submit a new claim", description = "Submits a new insurance claim against a policy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Claim submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "409", description = "Business rule violation")
    })
    public ResponseEntity<ClaimResponseDTO> submitClaim(@Valid @RequestBody ClaimRequestDTO requestDTO) {
        ClaimResponseDTO response = claimService.submitClaim(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Method to get a Claim
    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID", description = "Get claim by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Claim found"),
        @ApiResponse(responseCode = "404", description = "Claim not found")
    })
    public ResponseEntity<ClaimResponseDTO> getClaimById(@PathVariable Long id) {
        ClaimResponseDTO response = claimService.getClaimById(id);
        return ResponseEntity.ok(response);
    }

    // Method to get a Claim by a policyId
    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get claims by policy ID", description = "Get all claims for specific policy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Claims retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<List<ClaimResponseDTO>> getClaimsByPolicyId(@PathVariable Long policyId) {
        List<ClaimResponseDTO> response = claimService.getClaimsByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }

    // Method to update the status of  Claim
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update claim status", description = "Approves or rejects a claim")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Claim status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Claim not found"),
        @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public ResponseEntity<ClaimResponseDTO> updateClaimStatus(@PathVariable Long id, @Valid @RequestBody ClaimStatusUpdateDTO statusUpdateDTO) {
        ClaimResponseDTO response = claimService.updateClaimStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(response);
    }
}
