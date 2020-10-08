package com.tiket.sharing.fp.template;

import com.tiket.sharing.fp.model.OrderDetails;
import com.tiket.sharing.fp.model.OrderRequest;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
public interface CreateOrderHandler {
  Mono<OrderDetails> create(OrderRequest request, Consumer<CreateOrderOptions> customizer);
}
