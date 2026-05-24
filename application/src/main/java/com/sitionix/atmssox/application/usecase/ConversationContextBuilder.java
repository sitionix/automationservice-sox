package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ConversationContextBuilder {

    public UserAgentExecutionContext build(final String agentInstruction,
                                           final List<AgentRule> activeRules,
                                           final String summary,
                                           final Optional<ProjectRuntimeContext> projectRuntimeContext,
                                           final List<ConversationMessage> lastMessages,
                                           final ConversationMessage currentUserMessage) {
        final StringBuilder systemBuilder = new StringBuilder(AgentRuleTextNormalizer.normalizeToEmpty(agentInstruction));
        if (!activeRules.isEmpty()) {
            systemBuilder.append("\n\n");
            systemBuilder.append("Active rules:\n");
            systemBuilder.append(activeRules.stream()
                    .map(AgentRule::getContent)
                    .map(AgentRuleTextNormalizer::normalizeToEmpty)
                    .map(content -> "- " + content)
                    .collect(Collectors.joining("\n")));
        }
        final String normalizedSummary = AgentRuleTextNormalizer.normalizeToEmpty(summary);
        if (!normalizedSummary.isEmpty()) {
            systemBuilder.append("\n\n");
            systemBuilder.append("Conversation context summary:\n");
            systemBuilder.append(normalizedSummary);
        }

        final StringBuilder inputBuilder = new StringBuilder();
        this.appendProjectContextBlock(inputBuilder, projectRuntimeContext);
        inputBuilder.append("Messages:\n");
        for (ConversationMessage message : lastMessages) {
            inputBuilder.append(this.mapAuthor(message.getAuthorType()))
                    .append(": ")
                    .append(AgentRuleTextNormalizer.normalizeToEmpty(message.getContent()))
                    .append("\n");
        }
        if (this.shouldAppendCurrentMessage(lastMessages, currentUserMessage)) {
            inputBuilder.append("USER: ")
                    .append(AgentRuleTextNormalizer.normalizeToEmpty(currentUserMessage.getContent()))
                    .append("\n");
        }
        inputBuilder.append("Respond as AGENT to the latest USER message.");

        return new UserAgentExecutionContext(systemBuilder.toString(), inputBuilder.toString());
    }

    private void appendProjectContextBlock(final StringBuilder inputBuilder,
                                           final Optional<ProjectRuntimeContext> projectRuntimeContext) {
        if (projectRuntimeContext.isEmpty()) {
            return;
        }
        final ProjectRuntimeContext context = projectRuntimeContext.get();
        final String contextText = AgentRuleTextNormalizer.normalizeToEmpty(context.projectContextText());
        inputBuilder.append(contextText.isEmpty() ? "No additional project context provided." : contextText)
                .append("\n\n");
    }

    private boolean shouldAppendCurrentMessage(final List<ConversationMessage> lastMessages,
                                               final ConversationMessage currentUserMessage) {
        return currentUserMessage != null
                && currentUserMessage.getId() != null
                && lastMessages.stream().noneMatch(message -> currentUserMessage.getId().equals(message.getId()));
    }

    private String mapAuthor(final ConversationParticipantType authorType) {
        return authorType == ConversationParticipantType.USER ? "USER" : "AGENT";
    }
}
