package com.vi.appointmentservice.api.service.statistics.event;

import static com.vi.appointmentservice.helper.CustomLocalDateTime.toIsoTime;

import com.vi.appointmentservice.api.model.CalcomWebhookInputPayload;
import com.vi.appointmentservice.helper.CustomOffsetDateTime;
import com.vi.appointmentservice.helper.json.OffsetDateTimeToStringSerializer;
import com.vi.appointmentservice.statisticsservice.generated.web.model.EventType;
import com.vi.appointmentservice.statisticsservice.generated.web.model.BookingCreatedStatisticsEventMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingCreatedStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.BOOKING_CREATED;
  private final CalcomWebhookInputPayload payload;
  private final String consultantId;

  public BookingCreatedStatisticsEvent(CalcomWebhookInputPayload payload, String consultantId){
    this.payload = payload;
    this.consultantId = consultantId;
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
    var bookingCreatedStatisticsEventMessage =
        new BookingCreatedStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .userId(consultantId)
            .userRole(com.vi.appointmentservice.statisticsservice.generated.web.model.UserRole.CONSULTANT)
            .timestamp(CustomOffsetDateTime.nowInUtc())
            .type(payload.getType())
            .title(payload.getTitle())
            .startTime(toIsoTime(payload.getStartTime().toLocalDateTime()))
            .endTime(toIsoTime(payload.getEndTime().toLocalDateTime()))
            .uid(payload.getUid())
            .bookingId(payload.getBookingId()
        );

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(bookingCreatedStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
