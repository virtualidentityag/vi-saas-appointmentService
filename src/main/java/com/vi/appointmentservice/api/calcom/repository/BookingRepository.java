package com.vi.appointmentservice.api.calcom.repository;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

  private static final String USER_ID = "userId";
  private @Autowired
  NamedParameterJdbcTemplate calcomDBNamedParamterTemplate;

  public List<CalcomBooking> getConsultantActiveBookings(Long userId) {
    String query = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"userId\" = :userId AND now() < (\"startTime\" + INTERVAL '30 minutes') order by \"startTime\" ASC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue(USER_ID, userId);
    return calcomDBNamedParamterTemplate
        .query(query, parameters, new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getConsultantExpiredBookings(Long userId) {
    String query = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"userId\" = :userId AND now() > (\"startTime\" + INTERVAL '30 minutes') order by \"startTime\" DESC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue(USER_ID, userId);
    return calcomDBNamedParamterTemplate
        .query(query, parameters, new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getConsultantCancelledBookings(Long userId) {
    String query = "SELECT * FROM \"Booking\" AS booking WHERE booking.status = 'cancelled' AND "
        + "booking.\"userId\" = :userId order by \"startTime\" DESC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue(USER_ID, userId);
    return calcomDBNamedParamterTemplate
        .query(query, parameters, new CalcomRepositoryBookingMapper());
  }

  public CalcomBooking getBookingById(Long bookingId) {
    SqlParameterSource parameters = new MapSqlParameterSource("bookingId", bookingId);
    return calcomDBNamedParamterTemplate
        .queryForObject("select * from \"Booking\" where id = :bookingId", parameters,
            new CalcomRepositoryBookingMapper());
  }

  public List<CalcomBooking> getAskerActiveBookings(List<Long> bookingIds) {
    String query = "SELECT * FROM \"Booking\" AS booking WHERE booking.status != 'cancelled' AND "
        + "booking.\"id\" in (:ids) AND now() < (\"startTime\" + INTERVAL '30 minutes') order by \"startTime\" ASC";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("ids", bookingIds);
    return calcomDBNamedParamterTemplate
        .query(query, parameters, new CalcomRepositoryBookingMapper());
  }

  public Integer getBookingIdByUid(String uid) {
    String query = "SELECT \"id\" FROM \"Booking\" AS booking WHERE booking.\"uid\" = :uid LIMIT 1";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("uid", uid);

    return calcomDBNamedParamterTemplate
        .queryForObject(query, parameters, Integer.class);
  }

  public void deleteBooking(Long bookingId) {
    String query = "DELETE FROM \"Booking\" AS booking WHERE booking.\"id\" = :bookingId";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingId", bookingId);
    calcomDBNamedParamterTemplate.update(query, parameters);
  }

  public void deleteAttendeeWithoutBooking() {
    String query = "DELETE FROM \"Attendee\" AS attendee WHERE attendee.\"bookingId\" IS NULL";
    calcomDBNamedParamterTemplate.update(query, new MapSqlParameterSource());
  }

  public void updateAttendeeEmail(final List<Long> bookingIds, final String email) {
    String query = "UPDATE \"Attendee\" SET \"email\"=:email WHERE \"bookingId\" IN (:bookingIds)";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingIds", bookingIds)
        .addValue("email", email);
    calcomDBNamedParamterTemplate.update(query, parameters);
  }

  public CalcomBooking getBookingByUid(String bookingUid) {
    SqlParameterSource parameters = new MapSqlParameterSource("bookingUid", bookingUid);
    return calcomDBNamedParamterTemplate
        .queryForObject("select * from \"Booking\" where uid = :bookingUid", parameters,
            new CalcomRepositoryBookingMapper());
  }

  public void cancelBooking(String bookingUid) {
    String query = "UPDATE \"Booking\" SET status='cancelled' WHERE uid = :bookingUid";
    SqlParameterSource parameters = new MapSqlParameterSource()
        .addValue("bookingUid", bookingUid);
    calcomDBNamedParamterTemplate.update(query, parameters);
  }
}
