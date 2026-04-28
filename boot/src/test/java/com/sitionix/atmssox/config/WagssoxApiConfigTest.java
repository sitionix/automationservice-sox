package com.sitionix.atmssox.config;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.invoker.ApiClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WagssoxApiConfigTest {

    @Test
    void givenBasePath_whenCreateApiClient_thenSetBasePathAndDefaultJsonHeaders() {
        //given
        final WagssoxApiConfig givenConfig = new WagssoxApiConfig();
        givenConfig.setBasePath("http://wagssox.internal");

        //when
        final ApiClient actual = givenConfig.wagssoxClient();

        //then
        assertThat(actual.getBasePath()).isEqualTo("http://wagssox.internal");
    }

    @Test
    void givenApiClient_whenCreateSiteApi_thenReturnConfiguredSiteApi() {
        //given
        final WagssoxApiConfig givenConfig = new WagssoxApiConfig();
        final ApiClient givenApiClient = new ApiClient();

        //when
        final SiteApi actual = givenConfig.wagssoxSiteApi(givenApiClient);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.getApiClient()).isEqualTo(givenApiClient);
    }
}
