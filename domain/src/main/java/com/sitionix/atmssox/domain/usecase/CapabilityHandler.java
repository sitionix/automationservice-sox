package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.CapabilityExecutionResult;

/**
 * Defines one executable capability.
 */
public interface CapabilityHandler {

    /**
     * Describes capability metadata for tool registration/discovery.
     *
     * @return capability definition.
     */
    CapabilityDefinition definition();

    /**
     * Executes capability with a runtime command.
     *
     * @param command capability execution command.
     * @return execution result payload.
     */
    CapabilityExecutionResult execute(CapabilityExecutionCommand command);
}
