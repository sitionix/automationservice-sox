package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum CapabilityName {
    GET_SITE_OVERVIEW("getSiteOverviewCapabilityHandler");

    private final String bindingKey;

    @Setter
    private CapabilityHandler handler;

    public CapabilityDefinition definition() {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for capability: " + this.name());
        }
        return this.handler.definition();
    }

    public CapabilityExecutionResult execute(final CapabilityExecutionCommand command) {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for capability: " + this.name());
        }
        return this.handler.execute(command);
    }
}
