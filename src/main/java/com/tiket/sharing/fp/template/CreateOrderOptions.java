package com.tiket.sharing.fp.template;

import com.tiket.sharing.fp.model.OrderDetails;
import com.tiket.sharing.fp.model.OrderRequest;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
@Getter @Setter
@Accessors(chain = true, fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateOrderOptions {
  private Function<OrderRequest, Mono<OrderRequest>> requestPreprocessor;
  private Function<OrderDetails, Mono<Void>> orderSynchronizer;
  private UnaryOperator<Mono<OrderDetails>> orderPersister;

  public static CreateOrderOptions empty() {
    return new CreateOrderOptions();
  }
}
