package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;

/**
 * Defines one executable capability.
 */
public interface CapabilityHandler<H> {

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
    CapabilityExecutionResult execute(CapabilityExecutionCommand<H> command);

    /**
     * Runtime argument type expected by this capability.
     *
     * @return argument class for JSON deserialization.
     */
    Class<H> argType();
}
