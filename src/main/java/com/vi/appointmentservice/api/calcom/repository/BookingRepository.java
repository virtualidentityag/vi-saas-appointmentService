package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingRepository {

  private @Autowired
  NamedParameterJdbcTemplate calcomDBNamedParamterTemplate;

  public List<CalcomBooking> getConsultantActiveBookings(Long userId) {
    String QUERY = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"userId\" = :userId AND now() < \"startTime\" order by \"startTime\" ASC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("userId", userId);
    return calcomDBNamedParamterTemplate
        .query(QUERY, parameters, new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long userId) {
    String QUERY = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"userId\" = :userId AND now() > \"startTime\" order by \"startTime\" DESC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("userId", userId);
    return calcomDBNamedParamterTemplate
        .query(QUERY, parameters, new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long userId) {
    String QUERY = "SELECT * FROM \"Booking\" AS booking WHERE booking.status = 'cancelled' AND "
        + "booking.\"userId\" = :userId order by \"startTime\" DESC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("userId", userId);
    return calcomDBNamedParamterTemplate
        .query(QUERY, parameters, new CalcomRepositoryBookingMapper());
  }

  public CalcomBooking getBookingById(Long bookingId) {
    SqlParameterSource parameters = new MapSqlParameterSource("bookingId", bookingId);
    return calcomDBNamedParamterTemplate
        .queryForObject("select * from \"Booking\" where id = :bookingId", parameters,
            new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    String QUERY = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"id\" in (:ids) AND now() < \"startTime\" order by \"startTime\" ASC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("ids", bookingIds);
    return calcomDBNamedParamterTemplate
        .query(QUERY, parameters, new CalcomRepositoryBookingMapper());
  }

  public Integer getBookingIdByUid(String uid) {
    String QUERY = "SELECT \"id\" FROM \"Booking\" AS booking WHERE booking.\"uid\" = :uid LIMIT 1";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("uid", uid);

    return calcomDBNamedParamterTemplate
        .queryForObject(QUERY, parameters, Integer.class);
  }

  public void deleteBooking(Long bookingId) {
    String QUERY = "DELETE FROM \"Booking\" AS booking WHERE booking.\"id\" = :bookingId";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingId", bookingId);
    calcomDBNamedParamterTemplate.update(QUERY, parameters);
  }

  public void deleteAttendeeWithoutBooking() {
    String QUERY = "DELETE FROM \"Attendee\" AS attendee WHERE attendee.\"bookingId\" IS NULL";
    calcomDBNamedParamterTemplate.update(QUERY, new MapSqlParameterSource());
  }

  public void updateAttendeeEmail(final List<Long> bookingIds, final String email) {
    String QUERY = "UPDATE \"Attendee\" SET \"email\"=:email WHERE \"bookingId\" IN (:bookingIds)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingIds", bookingIds)
        .addValue("email", email);
    calcomDBNamedParamterTemplate.update(QUERY, parameters);
  }

  public CalcomBooking getBookingByUid(String bookingUid) {
    SqlParameterSource parameters = new MapSqlParameterSource("bookingUid", bookingUid);
    return calcomDBNamedParamterTemplate
        .queryForObject("select * from \"Booking\" where uid = :bookingUid", parameters,
            new CalcomRepositoryBookingMapper());
  }

  public void cancelBooking(String bookingUid) {
    String QUERY = "UPDATE \"Booking\" SET status='cancelled' WHERE uid = :bookingUid";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingUid", bookingUid);
    calcomDBNamedParamterTemplate.update(QUERY, parameters);
  }
}
