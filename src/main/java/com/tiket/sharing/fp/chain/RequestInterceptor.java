package com.tiket.sharing.fp.chain;

import static reactor.core.publisher.Mono.justOrEmpty;

import com.tiket.sharing.fp.chain.RequestInterceptor.InterceptorChain;
import com.tiket.sharing.fp.model.OrderRequest;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Demo for Chain of responsibility pattern.
 *
 * @author zakyalvan
 */
@FunctionalInterface
public interface RequestInterceptor {
  /**
   * Intercept request.
   *
   * @param request
   * @param chain
   * @return
   */
  Mono<OrderRequest> evaluate(OrderRequest request, InterceptorChain chain);

  @FunctionalInterface
  interface InterceptorChain {
    Mono<OrderRequest> evaluate(OrderRequest request);

    /**
     * Simple factory method for creating {@link InterceptorChain}, returning default implementation,
     * i.e. {@link DefaultInterceptorChain}.
     *
     * @param interceptors
     * @return
     */
    static InterceptorChain of(List<RequestInterceptor> interceptors) {
      return new DefaultInterceptorChain(interceptors);
    }
  }
}


/**
 * Default implementation of {@link InterceptorChain}.
 */
@Slf4j
class DefaultInterceptorChain implements InterceptorChain {
  private final Optional<RequestInterceptor> interceptor;
  private final Optional<InterceptorChain> next;

  DefaultInterceptorChain(List<RequestInterceptor> interceptors) {
    interceptors = (interceptors != null) ? interceptors : Collections.emptyList();

    ListIterator<? extends RequestInterceptor> iterator = interceptors
        .listIterator(interceptors.size());

    DefaultInterceptorChain chain = new DefaultInterceptorChain(null, null);
    while (iterator.hasPrevious()) {
      chain = new DefaultInterceptorChain(iterator.previous(), chain);
    }

    this.interceptor = chain.interceptor;
    this.next = chain.next;
  }
  DefaultInterceptorChain(RequestInterceptor interceptor, InterceptorChain next) {
    this.interceptor = Optional.ofNullable(interceptor);
    this.next = Optional.ofNullable(next);
  }

  /**
   * For last interceptor, we just return intercepted request object.
   *
   * @param request
   * @return
   */
  @Override
  public Mono<OrderRequest> evaluate(OrderRequest request) {
    return justOrEmpty(interceptor)
        .flatMap(intercept -> justOrEmpty(next)
            .flatMap(chain -> intercept.evaluate(request, chain))
        )
        .defaultIfEmpty(request);
  }
}
