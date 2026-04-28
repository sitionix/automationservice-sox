package com.sitionix.atmssox.client;

import com.sitionix.atmssox.domain.exception.ClientResponseException;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class WorkspaceProjectionClientCallExecutor {

    public <T> T execute(final Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpStatusCodeException exception) {
            throw new ClientResponseException(
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString(),
                    exception.getResponseHeaders(),
                    exception
            );
        }
    }
}
