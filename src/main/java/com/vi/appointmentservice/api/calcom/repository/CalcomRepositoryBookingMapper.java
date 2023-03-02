package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.RowMapper;

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
    return booking;
  }

  private String formatDate(String dateTime){
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString();
  }
}


