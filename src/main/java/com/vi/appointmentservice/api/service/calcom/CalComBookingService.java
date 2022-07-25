package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComBookingService extends CalComService {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull CalcomRepository calcomRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;

  @Autowired
  public CalComBookingService(RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull RescheduleHelper rescheduleHelper,
      CalcomRepository calcomRepository,
      CalcomBookingToAskerRepository calcomBookingToAskerRepository) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
    this.rescheduleHelper = rescheduleHelper;
    this.calcomRepository = calcomRepository;
    this.calcomBookingToAskerRepository = calcomBookingToAskerRepository;
  }

  // Booking
  public List<CalcomBooking> getAllBookings() {
    String response = this.restTemplate.getForObject(
        String.format(this.buildUri("/v1/bookings"), calcomApiUrl, calcomApiKey), String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("bookings").toString();
    ObjectMapper mapper = new ObjectMapper();
    List<CalcomBooking> result = null;
    try {
      result = List.of(Objects.requireNonNull(mapper.readValue(response, CalcomBooking[].class)));
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize bookings response from calcom api");
    }
    return result;
  }

  public List<CalcomBooking> getConsultantActiveBookings(Long consultantId) {
    return enrichResultSet(calcomRepository.getConsultantActiveBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long consultantId) {
    return enrichResultSet(calcomRepository.getConsultantExpiredBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long consultantId) {
    return enrichResultSet(calcomRepository.getConsultantCancelledBookings(consultantId));
  }

  private List<CalcomBooking> enrichResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      CalcomBookingToAsker calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if(calcomBookingAsker == null){
        log.error("Inconsistent data. Asker not found for booking.");
        continue;
      }
      booking.setAskerId(calcomBookingAsker.getAskerId());
      rescheduleHelper.attachRescheduleLink(booking);
      rescheduleHelper.attachAskerName(booking);
    }
    return bookings;
  }

  public CalcomBooking createBooking(CalcomBooking booking) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject bookingObject = new JSONObject(booking);
    log.debug("Creating booking: {}", bookingObject);
    HttpEntity<String> request = new HttpEntity<>(bookingObject.toString(), headers);

    return restTemplate.postForEntity(this.buildUri("/v1/bookings"), request, CalcomBooking.class)
        .getBody();
  }

  public CalcomBooking updateBooking(CalcomBooking booking) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject bookingObject = new JSONObject(booking);
    log.debug("Updating calcom user: {}", bookingObject);
    HttpEntity<String> request = new HttpEntity<>(bookingObject.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/bookings/" + booking.getId()), request,
        CalcomBooking.class).getBody();
  }


  public CalcomBooking getBookingById(Long bookingId) {
    String response = restTemplate.getForObject(
        String.format(this.buildUri("/v1/bookings/" + bookingId), calcomApiUrl, calcomApiKey),
        String.class);
    JSONObject jsonObject = new JSONObject(response);
    log.debug(String.valueOf(jsonObject));
    response = jsonObject.getJSONObject("booking").toString();
    log.debug(response);
    ObjectMapper mapper = new ObjectMapper();
    try {
      CalcomBooking calcomBooking = mapper.readValue(response, CalcomBooking.class);
      //TODO: change this to use zones properly
      calcomBooking.setStartTime(
          ZonedDateTime.parse(jsonObject.getJSONObject("booking").get("startTime").toString())
              .plusHours(2).toString());
      calcomBooking.setEndTime(
          ZonedDateTime.parse(jsonObject.getJSONObject("booking").get("endTime").toString())
              .plusHours(2).toString());
      return calcomBooking;
    } catch (JsonProcessingException e) {
      return null;
    }

  }
}
