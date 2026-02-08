package com.insurance.policymanagement.util;

import java.time.Year;
import java.util.Random;

public class NumberGenerator {
    
    private static final Random random = new Random();
    
    public static String generatePolicyNumber() {
        int currentYear = Year.now().getValue();
        int randomNumber = 100000 + random.nextInt(900000); // 6-digit random number
        return String.format("POL-%d-%06d", currentYear, randomNumber);
    }
    
    public static String generateClaimNumber() {
        int currentYear = Year.now().getValue();
        int randomNumber = 100000 + random.nextInt(900000); // 6-digit random number
        return String.format("CLM-%d-%06d", currentYear, randomNumber);
    }
}
