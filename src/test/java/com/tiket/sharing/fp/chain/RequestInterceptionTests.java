package com.tiket.sharing.fp.chain;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import com.tiket.sharing.fp.chain.RequestInterceptor.InterceptorChain;
import com.tiket.sharing.fp.model.OrderDetails;
import com.tiket.sharing.fp.model.OrderIdentifier;
import com.tiket.sharing.fp.model.OrderRequest;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = InterceptorTestConfiguration.class, webEnvironment = NONE)
class RequestInterceptionTests {
  @Autowired
  private InterceptorChain interceptorChain;

  @MockBean
  private CustomerOrderTracker orderTracker;

  @Test
  void whenAllCheckPassed_thenShouldSuccess() {
    when(orderTracker.duplicateOrder(any(OrderRequest.class)))
        .then(invocation -> Mono.empty());

    when(orderTracker.activeCount(any(String.class)))
        .then(invocation -> Mono.just(1));

    StepVerifier.create(interceptorChain.evaluate(ORDER_REQUEST))
        .expectSubscription().thenAwait()
        .assertNext(request -> {
          assertThat(request).isNotNull();
        })
        .expectComplete()
        .verify(Duration.ofSeconds(5));

    verify(orderTracker, times(1)).duplicateOrder(any(OrderRequest.class));
    verify(orderTracker, times(1)).activeCount(any());
  }

  @Test
  void whenActiveOrderCountReached_thenShouldFail() {
    when(orderTracker.duplicateOrder(any(OrderRequest.class)))
        .then(invocation -> Mono.empty());

    when(orderTracker.activeCount(any(String.class)))
        .then(invocation -> Mono.just(5));

    StepVerifier.create(interceptorChain.evaluate(ORDER_REQUEST))
        .expectSubscription().thenAwait()
        .expectErrorSatisfies(error -> {
          assertThat(RequestInterceptException.requestInterceptError(error)).isTrue();
          assertThat(error.getMessage()).isEqualTo("Maximum allowed order exceeded");
        })
        .verify(Duration.ofSeconds(5));

    verify(orderTracker, times(1)).duplicateOrder(any(OrderRequest.class));
    verify(orderTracker, times(1)).activeCount(any());
  }

  @Test
  void whenDuplicateOrderDetected_thenShouldFail() {
    when(orderTracker.duplicateOrder(any(OrderRequest.class)))
        .then(invocation -> Mono.just(OrderIdentifier.by("1234", "ASDQWERTZYU")));

    when(orderTracker.activeCount(any(String.class)))
        .then(invocation -> Mono.just(5));

    StepVerifier.create(interceptorChain.evaluate(ORDER_REQUEST))
        .expectSubscription().thenAwait()
        .expectErrorSatisfies(error -> {
          assertThat(RequestInterceptException.requestInterceptError(error)).isTrue();
          assertThat(error.getMessage()).isEqualTo("Double order detected with id 1234 and hash ASDQWERTZYU");
        })
        .verify(Duration.ofSeconds(5));

    verify(orderTracker, times(1)).duplicateOrder(any(OrderRequest.class));
    verify(orderTracker, never()).activeCount(any());
  }

  @Test
  void whenNoRequestInterceptor_thenShouldSuccess() {
    InterceptorChain emptyChain = InterceptorChain.of(emptyList());

    StepVerifier.create(emptyChain.evaluate(ORDER_REQUEST))
        .expectSubscription().thenAwait()
        .assertNext(interceptedRequest -> {
          assertThat(interceptedRequest).isEqualTo(ORDER_REQUEST);
        })
        .expectComplete()
        .verify(Duration.ofSeconds(5));
  }

  @AfterEach
  void tearDown() {
    reset(orderTracker);
  }

  private static final OrderRequest ORDER_REQUEST = OrderRequest.builder()
      .build();

  private static final OrderDetails ORDER_DETAILS = OrderDetails.builder()
      .build();
}