package com.sitionix.atmssox.application.usecase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "automation.conversation-execution")
public class ConversationExecutionProperties {

    private boolean runtimeDispatchEnabled = false;
}
