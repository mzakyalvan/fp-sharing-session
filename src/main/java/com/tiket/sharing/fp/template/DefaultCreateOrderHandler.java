package com.tiket.sharing.fp.template;

import com.tiket.sharing.fp.model.OrderDetails;
import com.tiket.sharing.fp.model.OrderRequest;
import com.tiket.sharing.fp.strategy.SupplierBookingAdapter;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
public class DefaultCreateOrderHandler extends CreateOrderSupport implements CreateOrderHandler {
  public DefaultCreateOrderHandler(SupplierBookingAdapter bookingAdapter) {
    super(bookingAdapter);
  }

  @Override
  public Mono<OrderDetails> create(OrderRequest request, Consumer<CreateOrderOptions> customizer) {
    return createOptions(customizer).flatMap(options -> createOrder(request, options));
  }
}
