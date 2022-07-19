package com.vi.appointmentservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.service.LogService;
import lombok.NoArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Customizes API error/exception handling to hide information and/or possible security
 * vulnerabilities.
 */
@NoArgsConstructor
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


  /**
   * Incoming request body could not be deserialized.
   *
   * @param ex      the exception to be handled
   * @param headers http headers
   * @param status  http status
   * @param request web request
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final HttpMessageNotReadableException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    LogService.logWarn(status, ex);

    return handleExceptionInternal(null, null, headers, status, request);
  }

  /**
   * @param ex      the exception to be handled
   * @param headers http headers
   * @param status  http status
   * @param request web request
   * @Valid on object fails validation.
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    LogService.logWarn(status, ex);

    return handleExceptionInternal(null, null, headers, status, request);
  }

  /**
   * 409 - Conflict.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({InvalidDataAccessApiUsageException.class, DataAccessException.class})
  protected ResponseEntity<Object> handleConflict(
      final RuntimeException ex, final WebRequest request) {
    LogService.logWarn(HttpStatus.CONFLICT, ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  /**
   * {@link RestTemplate} API client errors.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({HttpClientErrorException.class})
  protected ResponseEntity<Object> handleHttpClientException(
      final HttpClientErrorException ex, final WebRequest request) {
    LogService.logWarn(ex.getStatusCode(), ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), ex.getStatusCode(), request);
  }

  /**
   * 500 - Custom Internal Server Error with logging method.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({InternalServerErrorException.class})
  public ResponseEntity<Object> handleInternal(
      final InternalServerErrorException ex, final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(
        null, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * 400 - Custom Bad Request with logging method.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<Object> handleInternal(
      final BadRequestException ex, final WebRequest request) {
    LogService.logInternalServerError(ex);

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * 404 - Custom Not Found with logging method.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity<Object> handleInternal(
      final NotFoundException ex, final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * 500 - Custom Calcom Exception with logging method.
   *
   * @param ex      the exception to be handled
   * @param request web request
   */
  @ExceptionHandler({CalComApiException.class})
  public ResponseEntity<Object> handleInternal(
      final CalComApiException ex, final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(null, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }
}
