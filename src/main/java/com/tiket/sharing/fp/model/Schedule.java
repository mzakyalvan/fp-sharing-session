package com.tiket.sharing.fp.model;

import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class Schedule {

  @NotNull
  SupplierQualifier supplier;

  @NotBlank
  String origin;

  @NotBlank
  String destination;

  @NotNull
  @FutureOrPresent
  LocalDate departDate;

  LocalTime departTime;

  @NotNull
  @FutureOrPresent
  LocalDate arriveDate;

  LocalTime arriveTime;
  String trainNumber;
  String wagonClass;
  String subClass;
}