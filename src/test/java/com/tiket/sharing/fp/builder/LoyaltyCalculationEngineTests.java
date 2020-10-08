package com.tiket.sharing.fp.builder;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import com.tiket.sharing.fp.model.CustomerProfile;
import com.tiket.sharing.fp.model.MembershipTier;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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