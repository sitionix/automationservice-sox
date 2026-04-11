package com.sitionix.atmssox.it.infra;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.contract.ForgeDbContracts;
import com.sitionix.forgeit.domain.contract.DbContract;
import com.sitionix.forgeit.domain.contract.DbContractsDsl;
import com.sitionix.forgeit.domain.contract.clean.CleanupPolicy;

@ForgeDbContracts
public class DatabaseContract {

    public static final DbContract<AgentEntity> AGENT_ENTITY_DB_CONTRACT = DbContractsDsl.entity(AgentEntity.class)
            .cleanupPolicy(CleanupPolicy.DELETE_ALL)
            .build();

    private DatabaseContract() {
    }
}
