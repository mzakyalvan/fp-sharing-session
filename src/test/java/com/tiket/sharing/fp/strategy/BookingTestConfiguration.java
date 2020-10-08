package com.tiket.sharing.fp.strategy;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author zakyalvan
 */
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration({PropertyPlaceholderAutoConfiguration.class,
    MessageSourceAutoConfiguration.class, ValidationAutoConfiguration.class,
    JacksonAutoConfiguration.class, CodecsAutoConfiguration.class,
    WebClientAutoConfiguration.class})
@Import(SupplierBookingAdapter.BookingConfiguration.class)
class BookingTestConfiguration {

}
