package com.vi.appointmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomBooking;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CalComBookingService extends CalComService {

    @Autowired
    public CalComBookingService(RestTemplate restTemplate, @Value("${calcom.apiUrl}") String calcomApiUrl, @Value("${calcom.apiKey}") String calcomApiKey) {
        super(restTemplate, calcomApiUrl, calcomApiKey);
    }

    // Booking
    public List<CalcomBooking> getBookings() throws JsonProcessingException {
        String response = this.restTemplate.getForObject(String.format(this.buildUri("/v1/bookings"), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONArray("bookings").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        CalcomBooking[] result = mapper.readValue(response, CalcomBooking[].class);

        return List.of(Objects.requireNonNull(result));
    }


    public CalcomBooking createBooking(CalcomBooking booking) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject bookingObject = new JSONObject(booking);
        log.debug("Creating booking: {}", bookingObject);
        HttpEntity<String> request = new HttpEntity<>(bookingObject.toString(), headers);
        String askerIdParamPath = null;


        return restTemplate.postForEntity(this.buildUri("/v1/bookings"), request, CalcomBooking.class).getBody();
    }

    public CalcomBooking updateBooking(CalcomBooking booking) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject bookingObject = new JSONObject(booking);
        log.debug("Updating calcom user: {}", bookingObject);
        HttpEntity<String> request = new HttpEntity<>(bookingObject.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/bookings/" + booking.getId()), request, CalcomBooking.class).getBody();
    }


    public CalcomBooking getBookingById(Long bookingId) throws JsonProcessingException {
        String response = restTemplate.getForObject(String.format(this.buildUri("/v1/bookings/" + bookingId), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONObject("booking").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CalcomBooking.class);
    }
}
