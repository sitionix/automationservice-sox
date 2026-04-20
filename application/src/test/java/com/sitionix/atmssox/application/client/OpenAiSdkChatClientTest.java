package com.sitionix.atmssox.application.client;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ToolChoiceOptions;
import com.openai.services.blocking.ResponseService;
import com.sitionix.atmssox.application.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
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

    private OpenAiChatProperties openAiChatProperties;

    @BeforeEach
    void setUp() {
        this.openAiChatProperties = new OpenAiChatProperties();
        this.openAiChatProperties.setApiKey("test-key");
        this.openAiChatProperties.setModel("gpt-4.1-mini");
    }

    @Test
    void givenValidConfigurationAndSdkReply_whenExecute_thenReturnTrimmedReply() {
        //given
        final OpenAIClient openAIClient = Mockito.mock(OpenAIClient.class);
        final ResponseService responseService = Mockito.mock(ResponseService.class);
        final Response response = this.getResponseWithText("  Hello from assistant.  ");
        when(openAIClient.responses()).thenReturn(responseService);
        when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties) {
            @Override
            OpenAIClient createClient() {
                return openAIClient;
            }
        };

        //when
        final String actual = client.execute("instruction", "message");

        //then
        assertThat(actual).isEqualTo("Hello from assistant.");
        verify(openAIClient).responses();
        verify(responseService).create(any(ResponseCreateParams.class));
    }

    @Test
    void givenValidConfigurationAndEmptyReply_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        final OpenAIClient openAIClient = Mockito.mock(OpenAIClient.class);
        final ResponseService responseService = Mockito.mock(ResponseService.class);
        final Response response = this.getResponseWithText("   ");
        when(openAIClient.responses()).thenReturn(responseService);
        when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties) {
            @Override
            OpenAIClient createClient() {
                return openAIClient;
            }
        };

        //when
        //then
        assertThatThrownBy(() -> client.execute("instruction", "message"))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI returned empty reply");
    }

    @Test
    void givenMissingApiKey_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        this.openAiChatProperties.setApiKey(" ");
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties);

        //when
        //then
        assertThatThrownBy(() -> client.execute("instruction", "message"))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI API key is not configured");
    }

    @Test
    void givenMissingModel_whenExecute_thenThrowOpenAiExecutionException() {
        //given
        this.openAiChatProperties.setModel(" ");
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties);

        //when
        //then
        assertThatThrownBy(() -> client.execute("instruction", "message"))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI model is not configured");
    }

    @Test
    void givenUnexpectedSdkFailure_whenExecute_thenWrapAsOpenAiExecutionException() {
        //given
        final OpenAIClient openAIClient = Mockito.mock(OpenAIClient.class);
        final ResponseService responseService = Mockito.mock(ResponseService.class);
        when(openAIClient.responses()).thenReturn(responseService);
        when(responseService.create(any(ResponseCreateParams.class))).thenThrow(new RuntimeException("boom"));
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties) {
            @Override
            OpenAIClient createClient() {
                return openAIClient;
            }
        };

        //when
        //then
        assertThatThrownBy(() -> client.execute("instruction", "message"))
                .isInstanceOf(OpenAiExecutionException.class)
                .hasMessage("OpenAI request failed");
    }

    @Test
    void givenOptionalClientSettingsConfigured_whenCreateClient_thenBuildClientSuccessfully() {
        //given
        this.openAiChatProperties.setBaseUrl("http://localhost:8080/v1");
        this.openAiChatProperties.setOrgId("org_test");
        this.openAiChatProperties.setProjectId("proj_test");
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties);

        //when
        final OpenAIClient actual = client.createClient();

        //then
        assertThat(actual).isNotNull();
    }

    @Test
    void givenOptionalClientSettingsMissing_whenCreateClient_thenBuildClientSuccessfully() {
        //given
        this.openAiChatProperties.setBaseUrl(null);
        this.openAiChatProperties.setOrgId(null);
        this.openAiChatProperties.setProjectId(null);
        final OpenAiSdkChatClient client = new OpenAiSdkChatClient(this.openAiChatProperties);

        //when
        final OpenAIClient actual = client.createClient();

        //then
        assertThat(actual).isNotNull();
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
}
