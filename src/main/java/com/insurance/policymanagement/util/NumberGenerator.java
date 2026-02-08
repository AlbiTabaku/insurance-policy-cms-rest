package com.insurance.policymanagement.util;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

public class NumberGenerator {

    public static String generatePolicyNumber() {
        return generate("POL");
    }

    public static String generateClaimNumber() {
        return generate("CLM");
    }

    private static String generate(String prefix) {
        int year = Year.now().getValue();
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(100, 1000);

        return String.format("%s-%d-%d-%03d", prefix, year, timestamp, random);
    }
}
