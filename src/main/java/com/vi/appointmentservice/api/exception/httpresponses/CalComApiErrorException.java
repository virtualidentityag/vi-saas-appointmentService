package com.vi.appointmentservice.api.exception.httpresponses;

import com.vi.appointmentservice.api.service.LogService;
import java.util.function.Consumer;

public class CalComApiErrorException extends CustomHttpStatusException {

  private static final long serialVersionUID = -4160810917274267038L;

  public CalComApiErrorException(String message) {
    super(message, LogService::logWarn);
  }

  public CalComApiErrorException(String message, String arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public CalComApiErrorException(String message, Long arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public CalComApiErrorException(String message, String arg1, Long arg2) {
    super(String.format(message, arg1, arg2), LogService::logWarn);
  }

  public CalComApiErrorException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }

}
