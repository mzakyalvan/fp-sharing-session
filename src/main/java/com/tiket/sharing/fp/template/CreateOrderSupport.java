package com.tiket.sharing.fp.template;

import static reactor.core.publisher.Flux.fromStream;

import com.tiket.sharing.fp.model.BookingDetails;
import com.tiket.sharing.fp.model.BookingParameter;
import com.tiket.sharing.fp.model.OrderDetails;
import com.tiket.sharing.fp.model.OrderRequest;
import com.tiket.sharing.fp.model.PassengerProfile;
import com.tiket.sharing.fp.model.Schedule;
import com.tiket.sharing.fp.strategy.SupplierBookingAdapter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Template class responsible for creating order.
 *
 * @author zakyalvan
 */
abstract class CreateOrderSupport {
  private final SupplierBookingAdapter bookingAdapter;

  private Function<OrderRequest, Mono<OrderRequest>> requestPreprocessor;
  private Function<OrderDetails, Mono<Void>> orderSynchronizer;
  private UnaryOperator<Mono<OrderDetails>> orderPersister;

  public CreateOrderSupport(SupplierBookingAdapter bookingAdapter) {
    Assert.notNull(bookingAdapter, "Booking adapter must be provided");
    this.bookingAdapter = bookingAdapter;
  }

  protected Mono<OrderDetails> createOrder(OrderRequest orderRequest, CreateOrderOptions options) {
    return Mono.defer(() -> options.requestPreprocessor().apply(orderRequest))
        .flatMap(request -> Flux.fromStream(request.getSchedules().entrySet().stream().map(it -> Pair.of(it.getKey(), it.getValue())))
            .flatMapDelayError(pair -> Mono.fromCallable(BookingParameter::builder)
                .map(builder -> builder.customer(request.getCustomer())
                    .passengers(request.getPassengers())
                    .schedule(pair.getValue())
                    .build())
                .flatMap(bookingAdapter::create)
                .map(booking -> Pair.of(pair.getKey(), booking)), 2, 2
            )
            .collectMap(Pair::getKey, Pair::getValue)
            .map(bookings -> OrderDetails.builder()
                .build()
            )
        )
        .flatMap(order -> options.orderSynchronizer().apply(order).thenReturn(order))
        .transform(options.orderPersister());
  }

  protected Mono<CreateOrderOptions> createOptions(Consumer<CreateOrderOptions> customizer) {
    return Mono
        .fromCallable(() -> CreateOrderOptions.empty()
            .requestPreprocessor(this.requestPreprocessor)
            .orderSynchronizer(this.orderSynchronizer)
            .orderPersister(this.orderPersister)
        )
        .handle((options, sink) -> {
          if(!Objects.isNull(customizer)) {
            customizer.accept(options);
          }
          sink.next(options);
        });
  }
}
