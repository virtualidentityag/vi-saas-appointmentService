package com.vi.appointmentservice.config;

import com.vi.appointmentservice.messageservice.generated.web.MessageControllerApi;
import com.vi.appointmentservice.messageservice.generated.ApiClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MessageServiceApiClientConfig {

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  @Bean
  public MessageControllerApi messageControllerApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    ApiClient apiClient = new MessageApiClient(restTemplate);
    apiClient.setBasePath(this.messageServiceApiUrl);
    return new MessageControllerApi(apiClient);
  }

}