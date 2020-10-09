package com.tiket.sharing.fp.chain;

import static com.tiket.sharing.fp.chain.InterceptorTestConfiguration.DoubleOrderException.doubleOrderError;
import static com.tiket.sharing.fp.chain.InterceptorTestConfiguration.MaximumOrderException.maximumOrderError;

import com.tiket.sharing.fp.chain.RequestInterceptor.InterceptorChain;
import com.tiket.sharing.fp.model.OrderIdentifier;
import com.tiket.sharing.fp.model.OrderRequest;
import java.util.stream.Collectors;
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
    return (request, chain) -> orderTracker.duplicateOrder(request)
        .<OrderRequest>flatMap(identifier -> doubleOrderError(request, identifier))
        .switchIfEmpty(chain.evaluate(request));
  }

  @Bean
  @Order(1)
  RequestInterceptor activeCountChecker(CustomerOrderTracker orderTracker) {
    return (request, chain) -> orderTracker.activeCount(request.getCustomer().getEmailAddress())
        .filter(count -> count < 5)
        .switchIfEmpty(maximumOrderError(request))
        .then(chain.evaluate(request));
  }

  static class DoubleOrderException extends RequestInterceptException {
    private final OrderIdentifier duplicate;

    public DoubleOrderException(OrderRequest request, OrderIdentifier duplicate) {
      super(request, String.format("Double order detected with id %s and hash %s", duplicate.getId(), duplicate.getHash()), null);
      this.duplicate = duplicate;
    }

    public static <T> Mono<T> doubleOrderError(OrderRequest request, OrderIdentifier duplicate) {
      return Mono.error(new DoubleOrderException(request, duplicate));
    }

    public OrderIdentifier getDuplicate() {
      return duplicate;
    }
  }

  static class MaximumOrderException extends RequestInterceptException {
    public MaximumOrderException(OrderRequest request) {
      super(request, "Maximum allowed order exceeded", null);
    }

    public static <T> Mono<T> maximumOrderError(OrderRequest request) {
      return Mono.error(new MaximumOrderException(request));
    }
  }
}