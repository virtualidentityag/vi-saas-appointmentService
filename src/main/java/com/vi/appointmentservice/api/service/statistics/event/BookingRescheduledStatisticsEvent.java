package com.vi.appointmentservice.api.service.statistics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vi.appointmentservice.helper.CustomOffsetDateTime;
import com.vi.appointmentservice.helper.json.OffsetDateTimeToStringSerializer;
import com.vi.appointmentservice.statisticsservice.generated.web.model.BookingRescheduledStatisticsEventMessage;
import com.vi.appointmentservice.statisticsservice.generated.web.model.EventType;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookingRescheduledStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.BOOKING_RESCHEDULED;


  public BookingRescheduledStatisticsEvent(){
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
    var bookingRescheduledStatisticsEventMessage =
        new BookingRescheduledStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            //.userId()
            //.userRole()
            .timestamp(CustomOffsetDateTime.nowInUtc()
            );

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(bookingRescheduledStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
