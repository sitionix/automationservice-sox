package com.sitionix.atmssox.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostChatWorkflowDispatcher {

    private final List<PostChatWorkflow> workflows;

    public void dispatch(final ChatCompletedContext context) {
        log.debug(
                "Starting post-chat workflows dispatch for conversationId={}, agentId={}, workflowsCount={}",
                context.conversationId(),
                context.agentId(),
                this.workflows.size()
        );
        this.workflows.forEach(workflow -> {
            try {
                log.debug(
                        "Submitting post-chat workflow for conversationId={}, workflow={}",
                        context.conversationId(),
                        workflow.getClass().getSimpleName()
                );
                workflow.submit(context);
                log.debug(
                        "Post-chat workflow submitted for conversationId={}, workflow={}",
                        context.conversationId(),
                        workflow.getClass().getSimpleName()
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "Post-chat workflow failed for conversationId={}, workflow={}",
                        context.conversationId(),
                        workflow.getClass().getSimpleName(),
                        exception
                );
            }
        });
    }
}
