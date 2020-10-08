package com.tiket.sharing.fp.builder;

import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.constantEarning;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.LoyaltyCalculator.earningFactor;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.basicMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.goldMembership;
import static com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.platinumMembership;
import static java.math.BigDecimal.valueOf;
import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.util.StringUtils.hasText;

import com.tiket.sharing.fp.builder.LoyaltyCalculationEngine.DefaultCalculationEngine;
import com.tiket.sharing.fp.builder.LoyaltyCalculationEngineTests.CalculationTestConfiguration;
import com.tiket.sharing.fp.model.CustomerProfile;
import com.tiket.sharing.fp.model.MembershipTier;
import java.time.Duration;
import java.util.function.Predicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.test.StepVerifier;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = CalculationTestConfiguration.class, webEnvironment = NONE)
class LoyaltyCalculationEngineTests {
  @Autowired
  private LoyaltyCalculationEngine calculateEngine;

  @Autowired
  private TransactionTracker transactionTracker;

  @Test
  void whenCalculateForBasicTier_thenShouldSuccess() {
    when(transactionTracker.firstTransaction(any(CustomerProfile.class)))
        .thenReturn(false);

    StepVerifier
        .create(calculateEngine.calculator(BASIC_TIER_CUSTOMER).calculate(valueOf(10_000)))
        .expectSubscription().thenAwait()
        .assertNext(earning -> assertThat(earning.getPoints()).isEqualTo(200))
        .expectComplete()
        .verify(Duration.ofSeconds(3));
  }

  @Test
  void whenCalculateForNullTier_thenShouldSuccess() {
    when(transactionTracker.firstTransaction(any(CustomerProfile.class)))
        .thenReturn(false);

    StepVerifier
        .create(calculateEngine.calculator(NULL_TIER_CUSTOMER).calculate(valueOf(10_000)))
        .expectSubscription().thenAwait()
        .assertNext(earning -> assertThat(earning.getPoints()).isEqualTo(50))
        .expectComplete()
        .verify(Duration.ofSeconds(3));
  }

  @Test
  void whenCalculateForFirstTransaction_thenShouldSuccess() {
    when(transactionTracker.firstTransaction(any(CustomerProfile.class)))
        .thenReturn(true);

    StepVerifier
        .create(calculateEngine.calculator(BASIC_TIER_CUSTOMER).calculate(valueOf(10_000)))
        .expectSubscription().thenAwait()
        .assertNext(earning -> assertThat(earning.getPoints()).isEqualTo(275))
        .expectComplete()
        .verify(Duration.ofSeconds(3));
  }

  @Test
  void whenCalculateForTiketFamily_thenShouldSuccess() {
    when(transactionTracker.firstTransaction(any(CustomerProfile.class)))
        .thenReturn(true);

    StepVerifier
        .create(calculateEngine.calculator(BASIC_INTERNAL_CUSTOMER).calculate(valueOf(10_000)))
        .expectSubscription().thenAwait()
        .assertNext(earning -> assertThat(earning.getPoints()).isEqualTo(250))
        .expectComplete()
        .verify(Duration.ofSeconds(3));
  }

  @AfterEach
  void tearDown() {
    reset(transactionTracker);
  }

  @Configuration(proxyBeanMethods = false)
  static class CalculationTestConfiguration {
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
      return Mockito.mock(TransactionTracker.class);
    }
  }

  private static final CustomerProfile BASIC_TIER_CUSTOMER = CustomerProfile.builder()
      .title("Mr").fullName("Basic Tier Customer").memberTier(MembershipTier.BASIC)
      .emailAddress("asd@qwe.com")
      .build();

  private static final CustomerProfile BASIC_INTERNAL_CUSTOMER = CustomerProfile.builder()
      .title("Mr").fullName("Basic Tier Customer").memberTier(MembershipTier.BASIC)
      .emailAddress("pegawe@tiket.com")
      .build();

  private static final CustomerProfile NULL_TIER_CUSTOMER = CustomerProfile.builder()
      .title("Mr").fullName("Basic Tier Customer")
      .emailAddress("asd@rty.com")
      .build();
}