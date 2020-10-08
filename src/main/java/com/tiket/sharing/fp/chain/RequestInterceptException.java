package com.tiket.sharing.fp.chain;

import com.tiket.sharing.fp.model.OrderRequest;
import org.springframework.core.NestedRuntimeException;

/**
 * @author zakyalvan
 */
public class RequestInterceptException extends NestedRuntimeException {
  private final OrderRequest request;

  public RequestInterceptException(OrderRequest request, String message, Throwable cause) {
    super(message, cause);
    this.request = request;
  }

  public static boolean requestInterceptError(Throwable error) {
    return error instanceof RequestInterceptException;
  }
}
