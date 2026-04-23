package com.sitionix.atmssox.api.handler;

import com.app_afesox.atmssox.api_first.dto.ErrorDTO;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void setUp() {
        this.restExceptionHandler = new RestExceptionHandler();
    }

    @Test
    void givenAgentValidationException_whenHandleValidation_thenReturnBadRequest() {
        //given
        final AgentValidationException given = new AgentValidationException("Validation failed");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleValidation(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_REQUEST, "Validation failed"));
    }

    @Test
    void givenTransitionException_whenHandleTransition_thenReturnConflict() {
        //given
        final AgentLifecycleTransitionException given = new AgentLifecycleTransitionException("Invalid transition");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleTransition(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.CONFLICT, "Invalid transition"));
    }

    @Test
    void givenAgentChatNotAllowedException_whenHandleChatNotAllowed_thenReturnConflict() {
        //given
        final AgentChatNotAllowedException given = new AgentChatNotAllowedException("Only ACTIVE agent can execute chat");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleChatNotAllowed(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.CONFLICT, "Only ACTIVE agent can execute chat"));
    }

    @Test
    void givenNotFoundException_whenHandleNotFound_thenReturnNotFound() {
        //given
        final AgentNotFoundException given = new AgentNotFoundException("Agent not found");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleNotFound(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.NOT_FOUND, "Agent not found"));
    }

    @Test
    void givenAuthenticationRequiredException_whenHandleAuthenticationRequired_thenReturnUnauthorized() {
        //given
        final AuthenticationRequiredException given = new AuthenticationRequiredException("Authentication required");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleAuthenticationRequired(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    @Test
    void givenOpenAiExecutionException_whenHandleOpenAiExecutionException_thenReturnBadGateway() {
        //given
        final OpenAiExecutionException given = new OpenAiExecutionException("OpenAI request failed");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleOpenAiExecutionException(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_GATEWAY, "OpenAI request failed"));
    }

    @Test
    void givenStructuredOpenAiExecutionExceptionWithType_whenHandleOpenAiExecutionException_thenReturnUpstreamStatusAndType() {
        //given
        final OpenAiExecutionException given =
                new OpenAiExecutionException(429, "insufficient_quota", "quota_exceeded", "You exceeded your current quota.");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleOpenAiExecutionException(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.TOO_MANY_REQUESTS, "insufficient_quota", "You exceeded your current quota."));
    }

    @Test
    void givenStructuredOpenAiExecutionExceptionWithCodeOnly_whenHandleOpenAiExecutionException_thenReturnUpstreamStatusAndCode() {
        //given
        final OpenAiExecutionException given =
                new OpenAiExecutionException(401, null, "invalid_api_key", "Incorrect API key provided.");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleOpenAiExecutionException(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.UNAUTHORIZED, "invalid_api_key", "Incorrect API key provided."));
    }

    @Test
    void givenConstraintViolationException_whenHandleConstraintViolation_thenReturnFirstViolationMessage() {
        //given
        final ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        when(constraintViolation.getMessage()).thenReturn("Constraint failed");
        final ConstraintViolationException given = new ConstraintViolationException("Constraint failed", Set.of(constraintViolation));

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleConstraintViolation(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_REQUEST, "Constraint failed"));
    }

    @Test
    void givenMethodArgumentNotValidException_whenHandleMethodValidation_thenReturnFirstErrorMessage() {
        //given
        final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.reject("invalid", "Validation error message");
        final MethodArgumentNotValidException given =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleMethodValidation(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_REQUEST, "Validation error message"));
    }

    @Test
    void givenUnknownValidationException_whenHandleMethodValidation_thenReturnFallbackMessage() {
        //given
        final Exception given = new RuntimeException("Unexpected");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleMethodValidation(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_REQUEST, "Validation failed"));
    }

    @Test
    void givenHttpMessageNotReadableException_whenHandleNotReadable_thenReturnMalformedBodyMessage() {
        //given
        final HttpMessageNotReadableException given = new HttpMessageNotReadableException("Malformed");

        //when
        final ResponseEntity<ErrorDTO> actual = this.restExceptionHandler.handleNotReadable(given);

        //then
        assertThat(actual).isEqualTo(this.expectedError(HttpStatus.BAD_REQUEST, "Malformed request body"));
    }

    private ResponseEntity<ErrorDTO> expectedError(final HttpStatus status, final String details) {
        return this.expectedError(status, status.getReasonPhrase(), details);
    }

    private ResponseEntity<ErrorDTO> expectedError(final HttpStatus status, final String title, final String details) {
        return ResponseEntity.status(status)
                .body(ErrorDTO.builder()
                        .code(status.value())
                        .title(title)
                        .details(details)
                        .build());
    }
}
