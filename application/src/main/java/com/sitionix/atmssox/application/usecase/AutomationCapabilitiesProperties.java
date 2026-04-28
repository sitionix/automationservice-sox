package com.sitionix.atmssox.application.usecase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "automation.capabilities")
public class AutomationCapabilitiesProperties {

    private boolean enabled = true;

    private Discovery discovery = new Discovery();

    private Execution execution = new Execution();

    @Getter
    @Setter
    public static class Discovery {
        private int maxSelectedCapabilities = 5;
    }

    @Getter
    @Setter
    public static class Execution {
        private int maxDiscoveryCallsPerMessage = 1;
        private int maxCapabilityCallsPerMessage = 2;
    }
}
