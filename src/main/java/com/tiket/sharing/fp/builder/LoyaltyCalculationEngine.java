package com.tiket.sharing.fp.builder;

import static com.tiket.sharing.fp.model.LoyaltyEarning.create;
import static com.tiket.sharing.fp.model.MembershipTier.BASIC;
import static com.tiket.sharing.fp.model.MembershipTier.GOLD;
import static com.tiket.sharing.fp.model.MembershipTier.PLATINUM;
import static java.time.Duration.ofDays;
import static java.util.Objects.isNull;
import static reactor.core.publisher.Mono.fromCallable;

import com.tiket.sharing.fp.model.CustomerProfile;
import com.tiket.sharing.fp.model.LoyaltyEarning;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
@FunctionalInterface
public interface LoyaltyCalculationEngine {
  LoyaltyCalculator calculator(@NotNull CustomerProfile customer);

  @FunctionalInterface
  interface LoyaltyCalculator {

    /**
     * Calculate earning point based on transaction amount.
     *
     * @param amount
     * @return
     */
    Mono<LoyaltyEarning> calculate(BigDecimal amount);

    /**
     * Add result of other earning calculation result.
     *
     * @param other
     * @return
     */
    default LoyaltyCalculator thenAccumulate(LoyaltyCalculator other) {
      return amount -> calculate(amount)
          .flatMap(original -> other.calculate(amount)
              .map(additional -> create(original.getPoints() + additional.getPoints(),
                  Duration.ofDays(100)))
          );
    }

    static LoyaltyCalculator earningFactor(double factor, Duration validity) {
      return amount -> fromCallable(() -> create((int) (amount.longValue() * factor), validity));
    }
    static LoyaltyCalculator constantEarning(int points, Duration validity) {
      return amount -> fromCallable(() -> create(points, validity));
    }
  }

  @Validated
  class DefaultCalculationEngine implements LoyaltyCalculationEngine {
    private final Map<Predicate<CustomerProfile>, LoyaltyCalculator> calculateRules = new ConcurrentHashMap<>();
    private double defaultFactor;

    @Override
    public LoyaltyCalculator calculator(CustomerProfile customer) {
      return calculateRules.entrySet().stream()
          .filter(entry -> entry.getKey().test(customer))
          .map(Entry::getValue)
          .findFirst()
          .orElse(amount -> fromCallable(() -> create((int) (amount.longValue() * defaultFactor), ofDays(100))));
    }

    public DefaultCalculationEngine calculationRule(Predicate<CustomerProfile> predicate, LoyaltyCalculator calculator) {
      calculateRules.put(predicate, calculator);
      return this;
    }
    public DefaultCalculationEngine defaultFactor(double factor) {
      this.defaultFactor = factor;
      return this;
    }
  }

  static Predicate<CustomerProfile> basicMembership() {
    return customer -> !isNull(customer.getMemberTier()) && BASIC.equals(customer.getMemberTier());
  }
  static Predicate<CustomerProfile> goldMembership() {
    return customer -> !isNull(customer.getMemberTier()) && GOLD.equals(customer.getMemberTier());
  }
  static Predicate<CustomerProfile> platinumMembership() {
    return customer -> !isNull(customer.getMemberTier()) && PLATINUM.equals(customer.getMemberTier());
  }
}