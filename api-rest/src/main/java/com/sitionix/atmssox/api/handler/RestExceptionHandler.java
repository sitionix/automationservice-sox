package com.sitionix.atmssox.api.handler;

import com.app_afesox.atmssox.api_first.dto.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.exception.ClientResponseException;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.util.StringUtils;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(AgentValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidation(final AgentValidationException exception) {
        return this.asErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AgentLifecycleTransitionException.class)
    public ResponseEntity<ErrorDTO> handleTransition(final AgentLifecycleTransitionException exception) {
        return this.asErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AgentChatNotAllowedException.class)
    public ResponseEntity<ErrorDTO> handleChatNotAllowed(final AgentChatNotAllowedException exception) {
        return this.asErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(final AgentNotFoundException exception) {
        return this.asErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationRequired(final AuthenticationRequiredException exception) {
        return this.asErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(OpenAiExecutionException.class)
    public ResponseEntity<ErrorDTO> handleOpenAiExecutionException(final OpenAiExecutionException exception) {
        final int statusCode = exception.getHttpStatus();
        final String title = this.resolveOpenAiTitle(exception);
        return ResponseEntity.status(statusCode)
                .body(ErrorDTO.builder()
                        .code(statusCode)
                        .title(title)
                        .details(exception.getUpstreamMessage())
                        .build());
    }

    @ExceptionHandler(ClientResponseException.class)
    public ResponseEntity<ErrorDTO> handle(final ClientResponseException exception) {
        final HttpStatus status = HttpStatus.resolve(exception.getStatusCode());
        if (status == null) {
            log.warn("Unknown upstream status code: {}", exception.getStatusCode());
            return this.asErrorResponse(HttpStatus.BAD_GATEWAY, "Invalid upstream response status");
        }

        final ErrorDTO upstreamError = this.parseError(exception.getResponseBody());
        if (upstreamError != null) {
            return ResponseEntity.status(status).body(upstreamError);
        }
        return this.asErrorResponse(status, status.getReasonPhrase());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(final ConstraintViolationException exception) {
        final String details = exception.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(exception.getMessage());
        return this.asErrorResponse(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDTO> handleMethodValidation(final Exception exception) {
        if (exception instanceof HandlerMethodValidationException handlerMethodValidationException) {
            final Optional<String> details = handlerMethodValidationException.getAllValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream())
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .findFirst();
            return this.asErrorResponse(HttpStatus.BAD_REQUEST, details.orElse("Validation failed"));
        }
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            final String details = methodArgumentNotValidException.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .orElse("Validation failed");
            return this.asErrorResponse(HttpStatus.BAD_REQUEST, details);
        }
        return this.asErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleNotReadable(final HttpMessageNotReadableException exception) {
        return this.asErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    private ResponseEntity<ErrorDTO> asErrorResponse(final HttpStatus status, final String details) {
        return ResponseEntity.status(status)
                .body(ErrorDTO.builder()
                        .code(status.value())
                        .title(status.getReasonPhrase())
                        .details(details)
                        .build());
    }

    private String resolveOpenAiTitle(final OpenAiExecutionException exception) {
        if (StringUtils.hasText(exception.getUpstreamType())) {
            return exception.getUpstreamType();
        }
        if (StringUtils.hasText(exception.getUpstreamCode())) {
            return exception.getUpstreamCode();
        }
        return null;
    }

    private ErrorDTO parseError(final String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        try {
            return this.objectMapper.readValue(responseBody, ErrorDTO.class);
        } catch (Exception exception) {
            log.warn("Failed to parse upstream error body", exception);
            return null;
        }
    }
}
