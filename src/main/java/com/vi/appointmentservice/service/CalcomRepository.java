package com.vi.appointmentservice.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalcomRepository {

  private @Autowired JdbcTemplate calcomDBTemplate;

  public List<CalcomBooking> getAllBookingsByStatus(Long userId) {
    return calcomDBTemplate.query("select * from \"Booking\" as booking where booking.status != 'cancelled' and booking.\"userId\" = '" + userId+"'",
        new CalcomRepositoryBookingMapper());
  }

}
