package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComScheduleService extends CalComService {

  public CalComScheduleService(@NonNull RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public List<CalcomSchedule> getAllSchedules() {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/schedules"), String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("schedules").toString();
    ObjectMapper mapper = new ObjectMapper();
    List<CalcomSchedule> result = null;
    try {
      result = mapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize schedule response from calcom api");
    }
    return result;
  }

  public List<Integer> deleteAllSchedulesOfUser(Long userId) {
    ArrayList<Integer> scheduleList = new ArrayList<>();
    List<CalcomSchedule> schedulesOfUser = new ArrayList<>(this.getAllSchedules()).stream().filter(
            schedule -> schedule.getUserId() != null && schedule.getUserId() == userId.intValue())
        .collect(Collectors.toList());
    for (CalcomSchedule schedule : schedulesOfUser) {
      this.deleteSchedule(schedule.getId());
      scheduleList.add(schedule.getId());
    }
    return scheduleList;
  }

  public void deleteSchedule(int scheduleId) {
    restTemplate.exchange(this.buildUri("/v1/schedules/" + scheduleId), HttpMethod.DELETE, null,
        String.class).getStatusCode();
  }

}