package com.phorest.util;

import java.security.Principal;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrincipalUtils {
  public static String getPrincipalName(Principal principal) {
    return Optional.ofNullable(principal).map(Principal::getName).orElse("Anonymous");
  }
}
