package com.insurance.policymanagement.repository;

import com.insurance.policymanagement.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByPolicyId(Long policyId);
    
    boolean existsByClaimNumber(String claimNumber);
}
