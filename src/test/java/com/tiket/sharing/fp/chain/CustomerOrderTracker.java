package com.tiket.sharing.fp.chain;

import com.tiket.sharing.fp.model.OrderIdentifier;
import com.tiket.sharing.fp.model.OrderRequest;
import reactor.core.publisher.Mono;

/**
 * Simplified created/active order tracker.
 *
 * @author zakyalvan
 */
public interface CustomerOrderTracker {
  /**
   * Check whether given order request already placed, return identifier of previous order
   * if order considered duplicated.
   *
   * @param orderRequest
   * @return
   */
  Mono<OrderIdentifier> duplicateOrder(OrderRequest orderRequest);

  /**
   * Get number of active order of customer identified by given email.
   *
   * @param customerEmail
   * @return
   */
  Mono<Integer> activeCount(String customerEmail);
}