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
        this.workflows.forEach(workflow -> {
            try {
                workflow.submit(context);
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
