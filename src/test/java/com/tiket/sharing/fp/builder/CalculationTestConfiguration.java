package com.tiket.sharing.fp.builder;

import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.constantEarning;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.earningFactor;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.basicMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.goldMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.platinumMembership;
import static java.time.Duration.ofDays;
import static org.mockito.Mockito.mock;
import static org.springframework.util.StringUtils.hasText;

import com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.DefaultCalculationEngine;
import com.tiket.sharing.fp.model.CustomerProfile;
import java.util.function.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zakyalvan
 */
@Configuration(proxyBeanMethods = false)
class CalculationTestConfiguration {

  @Bean
  LoyaltyCalculationEngine loyaltyCalculationEngine(TransactionTracker transactionTracker) {
    Predicate<CustomerProfile> tiketFamilyMember = customer ->
        hasText(customer.getEmailAddress()) && customer.getEmailAddress().endsWith("@tiket.com");

    return new DefaultCalculationEngine()
        .calculationRule(basicMembership().and(transactionTracker::firstTransaction),
            earningFactor(.02, ofDays(120))
                .thenAccumulate(constantEarning(75, ofDays(100))))
        .calculationRule(basicMembership().and(tiketFamilyMember),
            earningFactor(.02, ofDays(120))
                .thenAccumulate(earningFactor(.005, ofDays(100))))
        .calculationRule(basicMembership(),
            earningFactor(.02, ofDays(120)))
        .calculationRule(goldMembership().and(tiketFamilyMember),
            earningFactor(.03, ofDays(150))
                .thenAccumulate(earningFactor(.008, ofDays(100))))
        .calculationRule(goldMembership(),
            earningFactor(.03, ofDays(150)))
        .calculationRule(platinumMembership().and(tiketFamilyMember),
            earningFactor(.04, ofDays(200))
                .thenAccumulate(earningFactor(0.01, ofDays(100))))
        .calculationRule(platinumMembership(),
            earningFactor(.04, ofDays(200)))
        .defaultFactor(.005);
  }

  @Bean
  TransactionTracker mockTransactionTracker() {
    return mock(TransactionTracker.class);
  }
}