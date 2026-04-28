package com.sitionix.atmssox.config;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.invoker.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class WagssoxApiConfigTest {

    private WagssoxApiConfig wagssoxApiConfig;

    @BeforeEach
    void setUp() {
        final RestTemplateBuilder givenRestTemplateBuilder = new RestTemplateBuilder();
        this.wagssoxApiConfig = new WagssoxApiConfig(givenRestTemplateBuilder);
    }

    @Test
    void givenBasePath_whenCreateApiClient_thenSetBasePathAndDefaultJsonHeaders() {
        //given
        this.wagssoxApiConfig.setBasePath("http://wagssox.internal");

        //when
        final ApiClient actual = this.wagssoxApiConfig.wagssoxClient(this.wagssoxApiConfig.wagssoxRestTemplate());

        //then
        assertThat(actual.getBasePath()).isEqualTo("http://wagssox.internal");
    }

    @Test
    void givenApiClient_whenCreateSiteApi_thenReturnConfiguredSiteApi() {
        //given
        final ApiClient givenApiClient = new ApiClient();

        //when
        final SiteApi actual = this.wagssoxApiConfig.wagssoxSiteApi(givenApiClient);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.getApiClient()).isEqualTo(givenApiClient);
    }

    @Test
    void givenNoInput_whenCreateRestTemplate_thenReturnNonNullTemplate() {
        //given

        //when
        final RestTemplate actual = this.wagssoxApiConfig.wagssoxRestTemplate();

        //then
        assertThat(actual).isNotNull();
    }
}
