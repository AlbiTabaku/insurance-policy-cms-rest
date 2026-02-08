package com.insurance.policymanagement.repository;
import com.insurance.policymanagement.model.Policy;
import org.springframework.data.jpa.domain.Specification;

public class PolicySpecifications {

    public static Specification<Policy> hasCustomerEmail(String customerEmail) {
        return (root, query, criteriaBuilder) -> {
            if (customerEmail == null || customerEmail.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customerEmail"), customerEmail);
        };
    }

    public static Specification<Policy> hasPolicyNumberContaining(String policyNumber) {
        return (root, query, criteriaBuilder) -> {
            if (policyNumber == null || policyNumber.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.upper(root.get("policyNumber")), "%" + policyNumber.toUpperCase() + "%"
            );
        };
    }

    public static Specification<Policy> hasStatus(Policy.PolicyStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Policy> hasPolicyType(Policy.PolicyType policyType) {
        return (root, query, criteriaBuilder) -> {
            if (policyType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("policyType"), policyType);
        };
    }
}

