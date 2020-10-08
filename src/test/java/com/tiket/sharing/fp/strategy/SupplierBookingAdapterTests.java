package com.tiket.sharing.fp.strategy;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiket.sharing.fp.model.BookingDetails;
import com.tiket.sharing.fp.model.BookingParameter;
import com.tiket.sharing.fp.model.BookingState;
import com.tiket.sharing.fp.model.CustomerProfile;
import com.tiket.sharing.fp.model.PassengerProfile;
import com.tiket.sharing.fp.model.BookPricing;
import com.tiket.sharing.fp.model.Schedule;
import com.tiket.sharing.fp.model.SupplierQualifier;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = BookingTestConfiguration.class, webEnvironment = NONE)
@MockServerSettings(ports = {6789, 9876}, perTestSuite = true)
class SupplierBookingAdapterTests {
  @Autowired
  private SupplierBookingAdapter bookingAdapter;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void whenCreateRailinkBookingWithValidParameters_thenShouldSuccess(MockServerClient mockServer) throws Exception {
    HttpRequest request = request("/railink/bookings")
        .withHeader("Content-Type", "application/json")
        .withHeader("Accept", "application/json")
        .withBody(JsonBody.json("{\n"
            + "      \"customer\" : {\n"
            + "        \"title\" : \"Mr\",\n"
            + "        \"fullName\" : \"Zaky Alvan\",\n"
            + "        \"emailAddress\" : \"zaky.alvan@tiket.com\",\n"
            + "        \"phoneNumber\" : \"6281320144088\"\n"
            + "      },\n"
            + "      \"passengers\" : [ {\n"
            + "        \"title\" : \"Mr\",\n"
            + "        \"fullName\" : \"Penumpang Gelap Kulitnya\"\n"
            + "      } ],\n"
            + "      \"schedule\" : {\n"
            + "        \"supplier\" : \"RAILINK\",\n"
            + "        \"origin\" : \"SDB\",\n"
            + "        \"destination\" : \"BST\",\n"
            + "        \"departDate\" : \"2020-10-09\",\n"
            + "        \"departTime\" : \"10:20:00\",\n"
            + "        \"arriveDate\" : \"2020-10-09\",\n"
            + "        \"arriveTime\" : \"11:30:00\",\n"
            + "        \"trainNumber\" : \"12345\",\n"
            + "        \"wagonClass\" : null,\n"
            + "        \"subClass\" : \"GG\"\n"
            + "      }\n"
            + "    }"));

    BookingDetails bookingDetails = BookingDetails.builder()
        .customer(TESTING_CUSTOMER).passengers(TESTING_PASSENGERS)
        .schedule(RAILINK_SCHEDULE)
        .pricing(BookPricing.builder()
            .basePrice(BigDecimal.TEN).convenienceFee(BigDecimal.ONE)
            .totalAmount(BigDecimal.TEN.add(BigDecimal.ONE))
            .build())
        .createdTime(LocalDateTime.now())
        .expiryTime(LocalDateTime.now().plusMinutes(30))
        .state(BookingState.BOOKED)
        .build();

    String responseJson = objectMapper.writeValueAsString(bookingDetails);

    HttpResponse response = response()
        .withHeader("Content-Type", "application/json")
        .withBody(JsonBody.json(responseJson));

    mockServer.when(request).respond(response);

    BookingParameter parameter = BookingParameter.builder()
        .customer(TESTING_CUSTOMER).passengers(TESTING_PASSENGERS).schedule(RAILINK_SCHEDULE)
        .build();

    StepVerifier.create(bookingAdapter.create(parameter))
        .expectSubscription()
        .assertNext(booking -> {
          assertThat(booking.getState()).isEqualTo(BookingState.BOOKED);
          assertThat(booking.getSchedule().getSupplier()).isEqualTo(SupplierQualifier.RAILINK);
        })
        .expectComplete()
        .verify(Duration.ofSeconds(5));
  }

  @AfterEach
  void tearDown(MockServerClient mockServer) {
    mockServer.reset();
  }

  private static final CustomerProfile TESTING_CUSTOMER = CustomerProfile.builder()
      .title("Mr").fullName("Zaky Alvan").emailAddress("zaky.alvan@tiket.com")
      .phoneNumber("6281320144088")
      .build();

  private static final List<PassengerProfile> TESTING_PASSENGERS = singletonList(
      PassengerProfile.builder()
          .title("Mr").fullName("Penumpang Gelap Kulitnya")
          .build());

  private static final Schedule RAILINK_SCHEDULE = Schedule.builder()
      .supplier(SupplierQualifier.RAILINK)
      .origin("SDB").destination("BST")
      .departDate(LocalDate.now().plusDays(1)).departTime(LocalTime.of(10,  20))
      .arriveDate(LocalDate.now().plusDays(1)).arriveTime(LocalTime.of(11,  30))
      .subClass("GG").trainNumber("12345")
      .build();

  private static final Schedule TRAIN_SCHEDULE = Schedule.builder()
      .supplier(SupplierQualifier.KAI)
      .origin("GMR").destination("BD")
      .departDate(LocalDate.now().plusDays(1)).departTime(LocalTime.of(10,  20))
      .arriveDate(LocalDate.now().plusDays(1)).arriveTime(LocalTime.of(13,  30))
      .wagonClass("EKS").subClass("A").trainNumber("4322")
      .build();
}