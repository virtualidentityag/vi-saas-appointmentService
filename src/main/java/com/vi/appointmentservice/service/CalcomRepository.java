package com.vi.appointmentservice.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalcomRepository {

  private @Autowired
  JdbcTemplate calcomDBTemplate;

  private @Autowired
  NamedParameterJdbcTemplate calcomDBNamedParamterTemplate;

  public List<CalcomBooking> getAllBookingsByStatus(Long userId) {
    return calcomDBTemplate.query(
        "select * from \"Booking\" as booking where booking.status != 'cancelled' and booking.\"userId\" = '"
            + userId + "'",
        new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getByIds(List<Long> bookingIds) {
    SqlParameterSource parameters = new MapSqlParameterSource("ids", bookingIds);
    return calcomDBNamedParamterTemplate.query("select * from \"Booking\" where id in (:ids)", parameters,
        new CalcomRepositoryBookingMapper());
  }


}
