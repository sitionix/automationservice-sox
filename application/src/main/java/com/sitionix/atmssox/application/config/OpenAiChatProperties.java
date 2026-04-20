package com.sitionix.atmssox.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai.chat")
public class OpenAiChatProperties {

    private String apiKey;

    private String model;

    private String baseUrl;

    private String orgId;

    private String projectId;
}
