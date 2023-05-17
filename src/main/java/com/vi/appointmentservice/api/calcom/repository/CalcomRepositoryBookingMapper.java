package com.vi.appointmentservice.api.calcom.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomBooking;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
public class CalcomRepositoryBookingMapper implements RowMapper<CalcomBooking> {

  @Override
  public CalcomBooking mapRow(ResultSet rs, int rowNum) throws SQLException {
    CalcomBooking booking = new CalcomBooking();
    booking.setId(rs.getLong("id"));
    booking.setTitle(rs.getString("title"));
    booking.setStartTime(formatDate(rs.getString("startTime")));
    booking.setEndTime(formatDate(rs.getString("endTime")));
    booking.setUserId(rs.getInt("userId"));
    booking.setEventTypeId(rs.getInt("eventTypeId"));
    booking.setUid(rs.getString("uid"));
    booking.setDescription(rs.getString("description"));
    booking.setLocation(rs.getString("location"));
    booking.setCancellationReason(rs.getString("cancellationReason"));

    if (rs.getString("metadata") != null) {
      booking.setMetadataUserId(getUserIdFromMetadata(rs, booking));
    }
    return booking;
  }

  private String getUserIdFromMetadata(ResultSet rs, CalcomBooking booking)
      throws SQLException {
    try {
      Map<String, Object> jsonMetadataMap  = new ObjectMapper().readValue(rs.getString("metadata"), new TypeReference<>() {});
      if (jsonMetadataMap != null && jsonMetadataMap.get("user") != null) {
        return jsonMetadataMap.get("user").toString();
      }
    } catch (JsonProcessingException e) {
      log.warn("Could not retrieve metadataUserId for calcom booking. Could not parse metadata for booking with id: " + booking.getId());
    }
    return null;
  }

  private String formatDate(String dateTime){
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString();
  }
}


