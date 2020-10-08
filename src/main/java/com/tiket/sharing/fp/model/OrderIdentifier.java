package com.tiket.sharing.fp.model;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.util.Assert;

/**
 * @author zakyalvan
 */
@Value
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderIdentifier implements Serializable {
  String id;
  String hash;

  public static OrderIdentifier by(String id, String hash) {
    Assert.hasText(id, "Order id must be provided");
    Assert.hasText(hash, "Order hash must be provided");

    return new OrderIdentifier(id, hash);
  }
}
