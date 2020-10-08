package com.tiket.sharing.fp.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Value;

/**
 * @author zakyalvan
 */
@Value
@Getter
@lombok.Builder(builderClassName = "Builder")
public class CustomerProfile {

  @NotBlank
  String title;

  @NotBlank
  String fullName;

  @Email
  @NotBlank
  String emailAddress;

  @NotBlank
  String phoneNumber;

  MembershipTier memberTier;
}