package com.sitionix.atmssox.config;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.invoker.ApiClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api.rest.client.wagssox")
public class WagssoxApiConfig {

    private String basePath;

    @Bean
    public ApiClient wagssoxClient() {
        final ApiClient apiClient = new ApiClient(new RestTemplate());
        apiClient.setBasePath(this.basePath);
        apiClient.addDefaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        apiClient.addDefaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return apiClient;
    }

    @Bean
    public SiteApi wagssoxSiteApi(final ApiClient wagssoxClient) {
        return new SiteApi(wagssoxClient);
    }
}
