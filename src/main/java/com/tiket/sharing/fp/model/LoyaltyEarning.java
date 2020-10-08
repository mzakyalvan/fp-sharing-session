package com.tiket.sharing.fp.model;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
public class LoyaltyEarning {
  Integer points;
  LocalDateTime expiryTime;

  public static LoyaltyEarning create(Integer points, Duration validity) {
    return new LoyaltyEarning(points, LocalDateTime.now().plus(validity));
  }
}
