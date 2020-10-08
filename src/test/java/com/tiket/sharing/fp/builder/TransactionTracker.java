package com.tiket.sharing.fp.builder;

import com.tiket.sharing.fp.model.CustomerProfile;

/**
 * Utility for testing purpose.
 *
 * @author zakyalvan
 */
public interface TransactionTracker {
  boolean firstTransaction(CustomerProfile customer);
}
