package com.sitionix.atmssox.postgresql.entity.conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_execution_failure_classes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatExecutionFailureClassEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "description", nullable = false, updatable = false, length = 64)
    private String description;
}
