package com.sitionix.atmssox.config;

import com.sitionix.forge.security.client.config.ForgeSecurityClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ForgeSecurityTargetsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void givenDefaultConfig_whenContextStarts_thenResolveWorkspaceTargetHost() {
        //given when then
        this.contextRunner.run(context -> {
            final ConfigurableEnvironment environment = context.getEnvironment();
            final ForgeSecurityClientProperties securityProperties = Binder.get(environment)
                    .bind("forge.security", Bindable.of(ForgeSecurityClientProperties.class))
                    .orElseThrow(() -> new IllegalStateException("Failed to bind forge.security"));

            assertThat(securityProperties.getServiceId()).isEqualTo("sitionixAutomation");
            assertThat(securityProperties.getTargets().get("sitionixWorkspace").getHost())
                    .isEqualTo("workspaceaggregationservice-sox");
        });
    }
}
