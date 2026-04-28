package com.sitionix.atmssox.client;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.core.http.Headers;
import com.openai.errors.OpenAIServiceException;
import com.openai.errors.RateLimitException;
import com.openai.errors.UnauthorizedException;
import com.openai.models.ErrorObject;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import com.openai.services.blocking.ResponseService;
import com.sitionix.atmssox.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiSdkChatClientTest {

    private static final OpenAiChatRequest DEFAULT_REQUEST = new OpenAiChatRequest("instruction", "message");

    private OpenAiChatProperties openAiChatProperties;

    private OpenAIClient openAIClient;

    private ResponseService responseService;

    @BeforeEach
    void setUp() {
        this.openAiChatProperties = new OpenAiChatProperties();
        this.openAiChatProperties.setApiKey("test-key");
        this.openAiChatProperties.setModel("gpt-4.1-mini");
        this.openAIClient = Mockito.mock(OpenAIClient.class);
        this.responseService = Mockito.mock(ResponseService.class);
        when(this.openAIClient.responses()).thenReturn(this.responseService);
    }

    @Test
    void givenValidConfigurationAndSdkReply_whenExecute_thenReturnTrimmedReply() {
        //given
        final Response response = this.getResponseWithText("  Hello from assistant.  ");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = this.createClient();

        //when
        final String actual = client.execute(DEFAULT_REQUEST);

        //then
        assertThat(actual).isEqualTo("Hello from assistant.");
        verify(this.openAIClient).responses();
        verify(this.responseService).create(any(ResponseCreateParams.class));
    }

    @Test
    void givenValidConfigurationAndEmptyReply_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        final Response response = this.getResponseWithText("   ");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI returned empty reply");
    }

    @Test
    void givenMissingApiKey_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        this.openAiChatProperties.setApiKey(" ");
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI API key is not configured");
    }

    @Test
    void givenMissingModel_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        this.openAiChatProperties.setModel(" ");
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI model is not configured");
    }

    @Test
    void givenUnexpectedSdkFailure_whenExecute_thenWrapAsOpenAiExecutionException() {
        //given
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(new RuntimeException("boom"));
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI request failed");
    }

    @Test
    void givenRateLimitErrorFromOpenAi_whenExecute_thenMapStructuredOpenAiExecutionException() {
        //given
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(
                RateLimitException.builder()
                        .headers(Headers.builder().build())
                        .error(ErrorObject.builder()
                                .type("insufficient_quota")
                                .code("insufficient_quota")
                                .param("messages")
                                .message("You exceeded your current quota.")
                                .build())
                        .build()
        );
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .satisfies(throwable -> {
                    final OpenAiExecutionException actual = (OpenAiExecutionException) throwable;
                    assertThat(actual.getHttpStatus()).isEqualTo(429);
                    assertThat(actual.getUpstreamType()).isEqualTo("insufficient_quota");
                    assertThat(actual.getUpstreamCode()).isEqualTo("insufficient_quota");
                    assertThat(actual.getUpstreamMessage()).isEqualTo("429: You exceeded your current quota.");
                });
    }

    @Test
    void givenUnauthorizedErrorFromOpenAi_whenExecute_thenMapStructuredOpenAiExecutionException() {
        //given
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(
                UnauthorizedException.builder()
                        .headers(Headers.builder().build())
                        .error(ErrorObject.builder()
                                .type("invalid_request_error")
                                .code("invalid_api_key")
                                .param("api_key")
                                .message("Incorrect API key provided.")
                                .build())
                        .build()
        );
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .satisfies(throwable -> {
                    final OpenAiExecutionException actual = (OpenAiExecutionException) throwable;
                    assertThat(actual.getHttpStatus()).isEqualTo(401);
                    assertThat(actual.getUpstreamType()).isEqualTo("invalid_request_error");
                    assertThat(actual.getUpstreamCode()).isEqualTo("invalid_api_key");
                    assertThat(actual.getUpstreamMessage()).isEqualTo("401: Incorrect API key provided.");
                });
    }

    @Test
    void givenOpenAiServiceExceptionWithoutTypeAndCode_whenExecute_thenMapOnlyRawExceptionFields() {
        //given
        final OpenAIServiceException serviceException = Mockito.mock(OpenAIServiceException.class);
        when(serviceException.statusCode()).thenReturn(429);
        when(serviceException.type()).thenReturn(Optional.empty());
        when(serviceException.code()).thenReturn(Optional.empty());
        when(serviceException.getMessage()).thenReturn("raw service message");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(serviceException);
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .satisfies(throwable -> {
                    final OpenAiExecutionException actual = (OpenAiExecutionException) throwable;
                    assertThat(actual.getHttpStatus()).isEqualTo(429);
                    assertThat(actual.getUpstreamType()).isNull();
                    assertThat(actual.getUpstreamCode()).isNull();
                    assertThat(actual.getUpstreamMessage()).isEqualTo("raw service message");
                });
    }

    @Test
    void givenOpenAiServiceExceptionWithInvalidBodyAndMessage_whenExecute_thenUseExceptionMessage() {
        //given
        final OpenAIServiceException serviceException = Mockito.mock(OpenAIServiceException.class);
        when(serviceException.statusCode()).thenReturn(500);
        when(serviceException.type()).thenReturn(Optional.empty());
        when(serviceException.code()).thenReturn(Optional.empty());
        when(serviceException.body()).thenReturn(JsonValue.from("not-a-json-object"));
        when(serviceException.getMessage()).thenReturn("provider transport failure");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(serviceException);
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .satisfies(throwable -> {
                    final OpenAiExecutionException actual = (OpenAiExecutionException) throwable;
                    assertThat(actual.getHttpStatus()).isEqualTo(500);
                    assertThat(actual.getUpstreamType()).isNull();
                    assertThat(actual.getUpstreamCode()).isNull();
                    assertThat(actual.getUpstreamMessage()).isEqualTo("provider transport failure");
                });
    }

    @Test
    void givenOpenAiServiceExceptionWithBlankMessage_whenExecute_thenKeepBlankMessageAsIs() {
        //given
        final OpenAIServiceException serviceException = Mockito.mock(OpenAIServiceException.class);
        when(serviceException.statusCode()).thenReturn(503);
        when(serviceException.type()).thenReturn(Optional.empty());
        when(serviceException.code()).thenReturn(Optional.empty());
        when(serviceException.getMessage()).thenReturn(" ");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenThrow(serviceException);
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.execute(DEFAULT_REQUEST))
                .isInstanceOf(OpenAiExecutionException.class)
                .satisfies(throwable -> {
                    final OpenAiExecutionException actual = (OpenAiExecutionException) throwable;
                    assertThat(actual.getHttpStatus()).isEqualTo(503);
                    assertThat(actual.getUpstreamMessage()).isEqualTo(" ");
                });
    }

    @Test
    void givenToolRequestWithToolResults_whenExecuteWithTools_thenReturnToolChatResponse() {
        //given
        final Response response = this.getResponseWithText("tool-mode-output");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = this.createClient();
        final CapabilityDefinition definition = new CapabilityDefinition(
                "GET_WORKSPACE_SITES",
                "desc",
                List.of("site"),
                CapabilityInputSchemaBuilder.objectSchema().build(),
                "out"
        );
        final OpenAiToolChatRequest request = new OpenAiToolChatRequest(
                "instruction",
                "input",
                "prev_1",
                List.of(definition),
                List.of(new OpenAiNativeToolResult("call_1", "{\"ok\":true}"))
        );

        //when
        final OpenAiToolChatResponse actual = client.executeWithTools(request);

        //then
        assertThat(actual.outputText()).isEqualTo("tool-mode-output");
        verify(this.responseService).create(any(ResponseCreateParams.class));
    }

    @Test
    void givenToolRequestWithoutToolResults_whenExecuteWithTools_thenReturnToolChatResponse() {
        //given
        final Response response = this.getResponseWithText("tool-mode-output");
        when(this.responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = this.createClient();
        final OpenAiToolChatRequest request = new OpenAiToolChatRequest(
                "instruction",
                "input",
                null,
                List.of(),
                List.of()
        );

        //when
        final OpenAiToolChatResponse actual = client.executeWithTools(request);

        //then
        assertThat(actual.outputText()).isEqualTo("tool-mode-output");
        verify(this.responseService).create(any(ResponseCreateParams.class));
    }

    @Test
    void givenNullToolRequest_whenExecuteWithTools_thenThrowOpenAiExecutionException() {
        //given
        final OpenAiSdkChatClient client = this.createClient();

        //when
        //then
        assertThatThrownBy(() -> client.executeWithTools(null))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI request is not configured");
    }

    private Response getResponseWithText(final String text) {
        final ResponseOutputText outputText = ResponseOutputText.builder()
                .text(text)
                .type(JsonValue.from("output_text"))
                .annotations(List.of())
                .build();

        final ResponseOutputMessage outputMessage = ResponseOutputMessage.builder()
                .id("msg_1")
                .role(JsonValue.from("assistant"))
                .status(ResponseOutputMessage.Status.COMPLETED)
                .type(JsonValue.from("message"))
                .addContent(outputText)
                .build();

        return Response.builder()
                .id("resp_1")
                .createdAt(1713600000d)
                .error(Optional.empty())
                .incompleteDetails(Optional.empty())
                .instructions(Optional.empty())
                .metadata(Optional.empty())
                .model("gpt-4.1-mini")
                .object_(JsonValue.from("response"))
                .addOutput(outputMessage)
                .parallelToolCalls(false)
                .temperature(Optional.empty())
                .toolChoice(ToolChoiceOptions.AUTO)
                .tools(List.of())
                .topP(Optional.empty())
                .background(Optional.empty())
                .completedAt(Optional.empty())
                .conversation(Optional.empty())
                .maxOutputTokens(Optional.empty())
                .maxToolCalls(Optional.empty())
                .previousResponseId(Optional.empty())
                .prompt(Optional.empty())
                .promptCacheRetention(Optional.empty())
                .reasoning(Optional.empty())
                .serviceTier(Optional.empty())
                .status(ResponseStatus.COMPLETED)
                .topLogprobs(Optional.empty())
                .truncation(Optional.empty())
                .build();
    }

    private OpenAiSdkChatClient createClient() {
        return new OpenAiSdkChatClient(
                this.openAIClient,
                this.openAiChatProperties,
                new OpenAiNativeToolAdapter(new ObjectMapper())
        );
    }
}
