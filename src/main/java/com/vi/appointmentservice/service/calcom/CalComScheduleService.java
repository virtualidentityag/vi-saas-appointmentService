package com.vi.appointmentservice.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomSchedule;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComScheduleService extends CalComService {

  public CalComScheduleService(@NonNull RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull ObjectMapper objectMapper) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public List<CalcomSchedule> getAllSchedules() throws JsonProcessingException {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/schedules"), String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("schedules").toString();
    ObjectMapper mapper = new ObjectMapper();
    List<CalcomSchedule> result = mapper.readValue(response,
        new TypeReference<List<CalcomSchedule>>() {
        });
    return result;
  }

  public List<Integer> deleteAllSchedulesOfUser(Long userId) throws JsonProcessingException {
    ArrayList<Integer> scheduleList = new ArrayList<>();
    List<CalcomSchedule> schedulesOfUser = new ArrayList<>(this.getAllSchedules()).stream()
        .filter(
            schedule -> schedule.getUserId() != null && schedule.getUserId() == userId.intValue())
        .collect(Collectors.toList());
    for (CalcomSchedule schedule : schedulesOfUser) {
      this.deleteSchedule(schedule.getId());
      scheduleList.add(schedule.getId());
    }
    return scheduleList;
  }

  public List<CalcomSchedule> getAllSchedulesOfUser(Long userId) throws JsonProcessingException {
    List<CalcomSchedule> result = this.getAllSchedules();
    return new ArrayList<>(result).stream()
        .filter(
            schedule -> schedule.getUserId() != null && schedule.getUserId() == userId.intValue())
        .collect(Collectors.toList());
  }


  public CalcomSchedule createSchedule(JSONObject schedule) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(schedule.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/schedules"), request, CalcomSchedule.class)
        .getBody();
  }

  public CalcomSchedule editSchedule(JSONObject schedule) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(schedule.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/schedules/" + schedule.get("id")), request,
        CalcomSchedule.class).getBody();
  }

  public void deleteSchedule(int scheduleId) {
    restTemplate.exchange(this.buildUri("/v1/schedules/" + scheduleId),
        HttpMethod.DELETE, null, String.class).getStatusCode();
  }

}