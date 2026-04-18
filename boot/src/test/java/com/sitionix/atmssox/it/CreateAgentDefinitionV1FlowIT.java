package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@IntegrationTest
class CreateAgentDefinitionV1FlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should create agent when description is missing")
    void givenMissingDescription_whenCreateAgent_thenPersistNullDescription() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .expectStatus(HttpStatus.CREATED)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setDescription(null)));

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .andExpected(entity -> Objects.nonNull(entity.getAgentId()))
                .assertEntity();
    }
}
