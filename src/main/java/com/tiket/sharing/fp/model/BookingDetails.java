package com.tiket.sharing.fp.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class BookingDetails {
  CustomerProfile customer;
  List<PassengerProfile> passengers;
  Schedule schedule;
  BookPricing pricing;
  BookingState state;
  LocalDateTime createdTime;
  LocalDateTime expiryTime;
}