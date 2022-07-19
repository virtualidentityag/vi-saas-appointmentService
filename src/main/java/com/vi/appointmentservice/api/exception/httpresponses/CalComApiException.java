package com.vi.appointmentservice.api.exception.httpresponses;

import com.vi.appointmentservice.api.service.LogService;
import java.util.function.Consumer;

public class CalComApiException extends CustomHttpStatusException {

  private static final long serialVersionUID = -4160810917274267038L;

  public CalComApiException(String message) {
    super(message, LogService::logWarn);
  }

  public CalComApiException(String message, String arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public CalComApiException(String message, Long arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public CalComApiException(String message, String arg1, Long arg2) {
    super(String.format(message, arg1, arg2), LogService::logWarn);
  }

  public CalComApiException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }

}
