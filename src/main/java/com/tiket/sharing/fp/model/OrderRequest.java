package com.tiket.sharing.fp.model;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderRequest {
  @Valid
  @NotNull
  CustomerProfile customer;

  @NotEmpty
  List<@Valid @NotNull PassengerProfile> passengers;

  @NotEmpty
  Map<JourneyDirection, @Valid @NotNull Schedule> schedules;
}