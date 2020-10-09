package com.tiket.sharing.fp.builder;

import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.constantEarning;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.earningFactor;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.basicMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.goldMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.platinumMembership;
import static java.time.Duration.ofDays;
import static org.mockito.Mockito.mock;
import static org.springframework.util.StringUtils.hasText;

import com.tiket.sharing.fp.model.CustomerProfile;
import java.util.function.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zakyalvan
 */
@Configuration(proxyBeanMethods = false)
class CalculationTestConfiguration {


  /**
   * Configure {@link LoyaltyCalculationEngine} to be used in our tests.
   *
   * Some of you might be wondering, how to make this dynamic, configurable using properties.
   * By slightly simple refactor, we could use Spring's expression language (SpEL) or Groovy scripting
   * for that purpose.
   *
   * @param transactionTracker
   * @return
   */
  @Bean
  LoyaltyCalculationEngine loyaltyCalculationEngine(TransactionTracker transactionTracker) {
    Predicate<CustomerProfile> tiketFamilyMember = customer ->
        hasText(customer.getEmailAddress()) && customer.getEmailAddress().endsWith("@tiket.com");

    return LoyaltyCalculationEngine.empty()
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