package com.vi.appointmentservice.api.calcom.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.model.CalcomBooking;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalcomRepositoryBookingMapperTest {

  @Mock
  ResultSet resultSet;
  @Test
  void mapRow_should_mapRowAndParseMetadataUserId() throws SQLException {
    // given

    when(resultSet.getLong("id")).thenReturn(1L);
    when(resultSet.getInt(Mockito.anyString())).thenReturn(2);
    when(resultSet.getString("title")).thenReturn("Title");
    when(resultSet.getString("description")).thenReturn("description");
    when(resultSet.getString("location")).thenReturn("location");
    when(resultSet.getString("cancellationReason")).thenReturn("cancellationReason");
    when(resultSet.getString("startTime")).thenReturn("2023-05-26 17:15:00");
    when(resultSet.getString("endTime")).thenReturn("2023-05-26 17:15:00");
    when(resultSet.getString("uid")).thenReturn("uid");
    when(resultSet.getString("metadata")).thenReturn("{\n"
        + "  \"user\": \"74e5b249-bd47-4e31-8a89-52e2b8475cd4\",\n"
        + "  \"rcToken\": \"rcToken\",\n"
        + "  \"rcUserId\": \"rcUserId\",\n"
        + "  \"sessionId\": \"1323\",\n"
        + "  \"userToken\": \"someToken\",\n"
        + "  \"videoCallUrl\": \"https://calcom-staging.suchtberatung.digital\",\n"
        + "  \"isInitialAppointment\": \"true\"\n"
        + "}");

    // when
    CalcomBooking calcomBooking = new CalcomRepositoryBookingMapper().mapRow(resultSet, 0);

    // then
    assertThat(calcomBooking.getMetadataUserId()).isEqualTo("74e5b249-bd47-4e31-8a89-52e2b8475cd4");
  }

  @Test
  void mapRow_should_mapRowAndNotParseMetadataUserIdIfMetadataJsonInvalid() throws SQLException {
    // given

    when(resultSet.getLong("id")).thenReturn(1L);
    when(resultSet.getInt(Mockito.anyString())).thenReturn(2);
    when(resultSet.getString("title")).thenReturn("Title");
    when(resultSet.getString("description")).thenReturn("description");
    when(resultSet.getString("location")).thenReturn("location");
    when(resultSet.getString("cancellationReason")).thenReturn("cancellationReason");
    when(resultSet.getString("startTime")).thenReturn("2023-05-26 17:15:00");
    when(resultSet.getString("endTime")).thenReturn("2023-05-26 17:15:00");
    when(resultSet.getString("uid")).thenReturn("uid");
    when(resultSet.getString("metadata")).thenReturn("not a json");

    // when
    CalcomBooking calcomBooking = new CalcomRepositoryBookingMapper().mapRow(resultSet, 0);

    // then
    assertThat(calcomBooking.getMetadataUserId()).isNull();
  }

}