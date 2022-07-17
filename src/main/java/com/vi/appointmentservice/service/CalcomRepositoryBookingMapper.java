package com.vi.appointmentservice.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CalcomRepositoryBookingMapper implements RowMapper<CalcomBooking> {

  @Override
  public CalcomBooking mapRow(ResultSet rs, int rowNum) throws SQLException {
    CalcomBooking booking = new CalcomBooking();
    booking.setId(rs.getInt("id"));
    booking.setTitle(rs.getString("title"));
    booking.setStartTime(rs.getString("startTime"));
    booking.setEndTime(rs.getString("endTime"));
    booking.setUserId(rs.getInt("userId"));
    booking.setEventTypeId(rs.getInt("eventTypeId"));
    booking.setUid(rs.getString("uid"));
    return booking;
  }
}
