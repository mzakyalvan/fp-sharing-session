package com.tiket.sharing.fp.model;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class BookPricing {
  BigDecimal basePrice;
  BigDecimal convenienceFee;
  BigDecimal totalAmount;
}
