package com.tiket.sharing.fp.model;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class BookingParameter {
  @Valid
  @NotNull
  CustomerProfile customer;

  @NotEmpty
  List<@Valid @NotNull PassengerProfile> passengers;

  @Valid
  @NotNull
  Schedule schedule;
}