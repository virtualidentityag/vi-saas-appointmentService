package com.vi.appointmentservice.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.vi.appointmentservice.appointmentservice.generated.web.AppointmentControllerApi;
import com.vi.appointmentservice.appointmentservice.generated.ApiClient;
@Component
public class VideoAppointmentServiceApiClientConfig {

  @Value("${video.appointment.service.api.url}")
  private String videoAppointmentServiceApiUrl;

  @Bean
  public AppointmentControllerApi appointmentControllerApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    ApiClient apiClient = new VideoAppointmentsApiClient(restTemplate);
    apiClient.setBasePath(this.videoAppointmentServiceApiUrl);
    return new AppointmentControllerApi(apiClient);
  }

}