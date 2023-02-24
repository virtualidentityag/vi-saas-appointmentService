package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComBookingService extends CalComService {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull BookingRepository bookingRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private @Value("${calcom.url}") String calcomUrl;

  @Autowired
  public CalComBookingService(RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull RescheduleHelper rescheduleHelper,
      BookingRepository bookingRepository,
      CalcomBookingToAskerRepository calcomBookingToAskerRepository) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
    this.rescheduleHelper = rescheduleHelper;
    this.bookingRepository = bookingRepository;
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
    return enrichConsultantResultSet(bookingRepository.getConsultantActiveBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long consultantId) {
    return enrichConsultantResultSet(bookingRepository.getConsultantExpiredBookings(consultantId));
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long consultantId) {
    return enrichConsultantResultSet(bookingRepository.getConsultantCancelledBookings(consultantId));
  }

  private List<CalcomBooking> enrichConsultantResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      Optional<CalcomBookingToAsker> calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if(!calcomBookingAsker.isPresent()){
        log.error("Inconsistent data. Asker not found for booking + " + booking.getId());
        continue;
      }
      CalcomBookingToAsker entity = calcomBookingAsker.get();
      booking.setVideoAppointmentId(entity.getVideoAppointmentId());
      booking.setAskerId(entity.getAskerId());
      rescheduleHelper.attachRescheduleLink(booking);
    }
    rescheduleHelper.attachAskerNames(bookings);
    return bookings;
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    return enrichAskerResultSet(bookingRepository.getAskerActiveBookings(bookingIds));
  }

  List<CalcomBooking> enrichAskerResultSet(List<CalcomBooking> bookings) {
    for (CalcomBooking booking : bookings) {
      Optional<CalcomBookingToAsker> calcomBookingAsker = calcomBookingToAskerRepository
          .findByCalcomBookingId(
              booking.getId());
      if(!calcomBookingAsker.isPresent()){
        log.error("Inconsistent data. Asker not found for booking + " + booking.getId());
        continue;
      }
      CalcomBookingToAsker entity = calcomBookingAsker.get();
      booking.setAskerId(entity.getAskerId());
      booking.setVideoAppointmentId(entity.getVideoAppointmentId());
      rescheduleHelper.attachRescheduleLink(booking);
    }
    rescheduleHelper.attachConsultantName(bookings);
    return bookings;
  }

  public CalcomBooking getBookingById(Long bookingId) {
    return bookingRepository.getBookingById(bookingId);
  }

  public void cancelBooking(String bookingUid) {
    JSONObject json = new JSONObject();
    json.put("allRemainingBookings", false);
    json.put("reason", "One of attendees of meeting deleted");
    json.put("uid", bookingUid);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);

    try {
      restTemplate
          .exchange(calcomUrl + "/api/cancel", HttpMethod.POST, request,
              String.class);
    } catch (Exception e) {
        log.info(e.getMessage());
    }
  }


}
