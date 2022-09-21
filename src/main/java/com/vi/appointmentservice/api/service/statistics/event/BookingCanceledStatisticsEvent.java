package com.vi.appointmentservice.api.service.statistics.event;

import static com.vi.appointmentservice.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vi.appointmentservice.api.model.CalcomWebhookInputPayload;
import com.vi.appointmentservice.helper.CustomOffsetDateTime;
import com.vi.appointmentservice.helper.json.OffsetDateTimeToStringSerializer;
import com.vi.appointmentservice.statisticsservice.generated.web.model.BookingCanceledStatisticsEventMessage;
import com.vi.appointmentservice.statisticsservice.generated.web.model.EventType;
import com.vi.appointmentservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingCanceledStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.BOOKING_CANCELLED;
  private final CalcomWebhookInputPayload payload;
  private final String consultantId;

  private final Integer bookingId;

  public BookingCanceledStatisticsEvent(CalcomWebhookInputPayload payload, String consultantId, Integer bookingId){
    this.payload = payload;
    this.consultantId = consultantId;
    this.bookingId = bookingId;
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.registerModule(buildSimpleModule());
  }

  private static SimpleModule buildSimpleModule() {
    return new SimpleModule()
        .addSerializer(OffsetDateTime.class, new OffsetDateTimeToStringSerializer());
  }

  /** {@inheritDoc} */
  public EventType getEventType() {
    return EVENT_TYPE;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getPayload() {
    var bookingCanceledStatisticsEventMessage =
        new BookingCanceledStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .userId(consultantId)
            .userRole(UserRole.CONSULTANT)
            .timestamp(CustomOffsetDateTime.nowInUtc())
            .uid(payload.getUid())
            .bookingId(bookingId)
            .prevBookingId(bookingId);

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(bookingCanceledStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
