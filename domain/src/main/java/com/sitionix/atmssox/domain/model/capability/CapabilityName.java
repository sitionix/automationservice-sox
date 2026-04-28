package com.sitionix.atmssox.domain.model.capability;

import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum CapabilityName {
    GET_SITE_OVERVIEW("getSiteOverviewCapabilityHandler"),
    GET_WORKSPACE_SITES("getWorkspaceSitesCapabilityHandler");

    private final String bindingKey;

    @Setter
    private CapabilityHandler<Object> handler;

    public CapabilityDefinition definition() {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for capability: " + this.name());
        }
        return this.handler.definition();
    }

    public CapabilityExecutionResult execute(final CapabilityExecutionCommand<Object> command) {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for capability: " + this.name());
        }
        return this.handler.execute(command);
    }

    public Class<Object> argType() {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for capability: " + this.name());
        }
        return this.handler.argType();
    }
}
