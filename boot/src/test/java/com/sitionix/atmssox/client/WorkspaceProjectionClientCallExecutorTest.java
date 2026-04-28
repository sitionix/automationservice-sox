package com.sitionix.atmssox.client;

import com.sitionix.atmssox.domain.exception.ClientResponseException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceProjectionClientCallExecutorTest {

    private final WorkspaceProjectionClientCallExecutor workspaceProjectionClientCallExecutor = new WorkspaceProjectionClientCallExecutor();

    @Test
    void givenSuccessfulSupplier_whenExecute_thenReturnSupplierResult() {
        //given

        //when
        final String actual = this.workspaceProjectionClientCallExecutor.execute(() -> "ok");

        //then
        assertThat(actual).isEqualTo("ok");
    }

    @Test
    void givenHttpStatusCodeException_whenExecute_thenThrowClientResponseExceptionWithSamePayload() {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add("x-trace-id", "trace-1");
        final HttpClientErrorException givenException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                headers,
                "{\"code\":400,\"title\":\"Bad Request\",\"details\":\"Invalid siteId\"}".getBytes(),
                null
        );

        //when
        //then
        assertThatThrownBy(() -> this.workspaceProjectionClientCallExecutor.execute(() -> {
            throw givenException;
        }))
                .isInstanceOf(ClientResponseException.class)
                .satisfies(throwable -> {
                    final ClientResponseException actual = (ClientResponseException) throwable;
                    assertThat(actual.getStatusCode()).isEqualTo(400);
                    assertThat(actual.getResponseBody()).contains("Invalid siteId");
                    assertThat(actual.getResponseHeaders().get("x-trace-id")).isEqualTo(List.of("trace-1"));
                });
    }
}
