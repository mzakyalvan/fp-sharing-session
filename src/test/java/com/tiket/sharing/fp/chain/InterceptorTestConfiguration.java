package com.tiket.sharing.fp.chain;

import com.tiket.sharing.fp.chain.RequestInterceptor.InterceptorChain;
import com.tiket.sharing.fp.model.OrderIdentifier;
import com.tiket.sharing.fp.model.OrderRequest;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * Testing configuration for {@link RequestInterceptor} components.
 *
 * @author zakyalvan
 */
@Configuration(proxyBeanMethods = false)
class InterceptorTestConfiguration {
  @Bean
  InterceptorChain interceptorChain(ObjectProvider<RequestInterceptor> requestInterceptors) {
    return InterceptorChain.of(requestInterceptors.stream()
        .sorted(AnnotationAwareOrderComparator.INSTANCE)
        .collect(Collectors.toList()));
  }

  @Bean
  @Order(0)
  RequestInterceptor doubleOrderChecker(CustomerOrderTracker orderTracker) {
    return (request, chain) -> Mono.just(request)
        .flatMap(it -> orderTracker.duplicateOrder(it)
            .flatMap(identifier -> DoubleOrderException.<OrderRequest>doubleOrderError(it, identifier))
        )
        .switchIfEmpty(chain.evaluate(request));
  }

  @Bean
  @Order(1)
  RequestInterceptor activeCountChecker(CustomerOrderTracker orderTracker) {
    return (request, chain) -> Mono.just(request)
        .filterWhen(it -> orderTracker.activeCount("")
            .map(count -> count < 5))
        .switchIfEmpty(MaximumOrderException.maximumOrderError(request))
        .flatMap(chain::evaluate);
  }

  @Getter
  static class DoubleOrderException extends RequestInterceptException {
    private final OrderIdentifier duplicate;

    public DoubleOrderException(OrderRequest request, OrderIdentifier duplicate) {
      super(request, String.format("Double order detected with id %s and hash %s", duplicate.getId(), duplicate.getHash()), null);
      this.duplicate = duplicate;
    }

    public static <T> Mono<T> doubleOrderError(OrderRequest request, OrderIdentifier duplicate) {
      return Mono.error(new DoubleOrderException(request, duplicate));
    }
  }

  @Getter
  static class MaximumOrderException extends RequestInterceptException {
    public MaximumOrderException(OrderRequest request) {
      super(request, "Maximum allowed order exceeded", null);
    }

    public static <T> Mono<T> maximumOrderError(OrderRequest request) {
      return Mono.error(new MaximumOrderException(request));
    }
  }
}