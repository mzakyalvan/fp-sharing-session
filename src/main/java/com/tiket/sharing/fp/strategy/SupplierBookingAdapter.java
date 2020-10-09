package com.tiket.sharing.fp.strategy;

import static java.util.stream.Collectors.toList;

import com.tiket.sharing.fp.model.BookingDetails;
import com.tiket.sharing.fp.model.BookingParameter;
import com.tiket.sharing.fp.model.SupplierQualifier;
import java.time.Duration;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

/**
 * Contract for type responsible for create booking to supplier.
 *
 * @author zakyalvan
 */
@FunctionalInterface
public interface SupplierBookingAdapter {
  /**
   * Place/create a booking to supplier.
   *
   * @param parameter
   * @return
   */
  Mono<BookingDetails> create(BookingParameter parameter);

  /**
   * Extension of {@link SupplierBookingAdapter} contract, enable us to select on runtime,
   * i.e. based on given {@link BookingParameter}.
   */
  interface SupplierBookingDelegate extends SupplierBookingAdapter {

    /**
     * Check whether delegate handler supports/can process given parameter.
     *
     * @param parameter
     * @return
     */
    Boolean supports(@NotNull @Valid BookingParameter parameter);
  }

  @Validated
  class DelegatingBookingAdapter implements SupplierBookingAdapter  {
    private final List<SupplierBookingDelegate> delegates;

    public DelegatingBookingAdapter(List<SupplierBookingDelegate> delegates) {
      this.delegates = delegates;
    }

    @Override
    public Mono<BookingDetails> create(BookingParameter parameter) {
      return Mono
          .justOrEmpty(delegates.stream()
              .filter(delegate -> delegate.supports(parameter))
              .findFirst())
          .switchIfEmpty(BookingException.noAdapterError(parameter))
          .flatMap(delegate -> delegate.create(parameter));
    }
  }

  /**
   * Assume each client supplier receive same booking parameter for simplicity.
   */
  @Validated
  class RailinkBookingDelegate implements SupplierBookingDelegate {
    private final WebClient webClient;

    public RailinkBookingDelegate(@NotNull WebClient webClient) {
      this.webClient = webClient;
    }

    @Override
    public Boolean supports(BookingParameter parameter) {
      return SupplierQualifier.RAILINK.equals(parameter.getSchedule().getSupplier());
    }

    @Override
    public Mono<BookingDetails> create(BookingParameter parameter) {
      return webClient.post()
          .uri(builder -> builder.path("/railink/bookings")
              .build())
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(parameter)
          .retrieve().bodyToMono(BookingDetails.class)
          .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
              .maxBackoff(Duration.ofMillis(1000)).jitter(.1)
              .onRetryExhaustedThrow((spec, signal) -> signal.failure())
              .filter(error -> error instanceof PrematureCloseException));
    }
  }

  /**
   * Assume each client supplier receive same booking parameter for simplicity.
   */
  @Validated
  class TrainBookingDelegate implements SupplierBookingDelegate {
    private final WebClient webClient;

    public TrainBookingDelegate(@NotNull WebClient webClient) {
      this.webClient = webClient;
    }

    @Override
    public Boolean supports(BookingParameter parameter) {
      return SupplierQualifier.KAI.equals(parameter.getSchedule().getSupplier());
    }

    @Override
    public Mono<BookingDetails> create(BookingParameter parameter) {
      return webClient.post()
          .uri(builder -> builder.path("/kai/bookings")
              .build())
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(parameter)
          .retrieve().bodyToMono(BookingDetails.class)
          .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
              .maxBackoff(Duration.ofSeconds(1)).jitter(.1)
              .onRetryExhaustedThrow((spec, signal) -> signal.failure())
              .filter(error -> error instanceof PrematureCloseException));
    }
  }

  /**
   * Configuration for testing purpose.
   */
  @Configuration(proxyBeanMethods = false)
  class BookingConfiguration {
    @Bean
    @Primary
    SupplierBookingAdapter bookingAdapter(ObjectProvider<SupplierBookingDelegate> delegateProvider) {
      return new DelegatingBookingAdapter(delegateProvider.stream().collect(toList()));
    }

    @Bean
    SupplierBookingDelegate railinkDelegate(WebClient.Builder webClients) {
      WebClient webClient = webClients.clone().baseUrl("http://localhost:9876").build();
      return new RailinkBookingDelegate(webClient);
    }

    @Bean
    SupplierBookingAdapter trainBookingDelegate(WebClient.Builder webClients) {
      WebClient webClient = webClients.clone().baseUrl("http://localhost:6789").build();
      return new TrainBookingDelegate(webClient);
    }
  }

  /**
   * Error to be thrown when booking process failed.
   */
  @Getter
  class BookingException extends NestedRuntimeException {
    private final BookingParameter parameter;

    BookingException(BookingParameter parameter, String message, Throwable cause) {
      super(message, cause);
      this.parameter = parameter;
    }

    public static <T> Mono<T> noAdapterError(BookingParameter parameter) {
      return Mono.error(new BookingException(parameter, "No booking adapter", null));
    }
    public static <T> Mono<T> unknownBookingError(BookingParameter parameter, Throwable cause) {
      return Mono.error(new BookingException(parameter, "Unknown booking error", cause));
    }

    public static boolean bookingError(Throwable throwable)  {
      return throwable instanceof BookingException;
    }
  }
}
