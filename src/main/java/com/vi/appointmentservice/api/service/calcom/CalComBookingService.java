package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    return enrichConsultantResultSet(calcomRepository.getConsultantActiveBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long consultantId) {
    return enrichConsultantResultSet(calcomRepository.getConsultantExpiredBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long consultantId) {
    return enrichConsultantResultSet(calcomRepository.getConsultantCancelledBookings(consultantId));
  }

  private List<CalcomBooking> enrichConsultantResultSet(List<CalcomBooking> bookings) {
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
      try{
        rescheduleHelper.attachAskerName(booking);
      }catch (Exception e){
        //TODO: tmp fix until we see how deletion workflow affects users
      }

    }
    return bookings;
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    return enrichAskerResultSet(calcomRepository.getAskerActiveBookings(bookingIds));
  }

  List<CalcomBooking> enrichAskerResultSet(List<CalcomBooking> bookings) {
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
      rescheduleHelper.attachConsultantName(booking);
    }
    return bookings;
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
      calcomBooking.setStartTime(jsonObject.getJSONObject("booking").get("startTime").toString());
      calcomBooking.setEndTime(jsonObject.getJSONObject("booking").get("endTime").toString());
      return calcomBooking;
    } catch (JsonProcessingException e) {
      return null;
    }

  }
}
