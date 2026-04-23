package com.sitionix.atmssox.domain.model;

public enum AgentRuleStatus {

    ACTIVE,
    DELETED;

    public AgentRuleStatus delete() {
        return DELETED;
    }
}
