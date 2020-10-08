package com.tiket.sharing.fp.model;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class PassengerProfile {
  @NotBlank
  String title;

  @NotBlank
  String fullName;
}