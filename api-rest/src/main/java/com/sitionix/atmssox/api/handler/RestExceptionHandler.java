package com.sitionix.atmssox.api.handler;

import com.app_afesox.atmssox.api_first.dto.ErrorDTO;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Optional;

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

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AgentValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidation(final AgentValidationException exception) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AgentLifecycleTransitionException.class)
    public ResponseEntity<ErrorDTO> handleTransition(final AgentLifecycleTransitionException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AgentChatNotAllowedException.class)
    public ResponseEntity<ErrorDTO> handleChatNotAllowed(final AgentChatNotAllowedException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(final AgentNotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationRequired(final AuthenticationRequiredException exception) {
        return buildError(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(OpenAiExecutionException.class)
    public ResponseEntity<ErrorDTO> handleOpenAiExecutionException(final OpenAiExecutionException exception) {
        final HttpStatus status = HttpStatus.resolve(exception.getHttpStatus()) == null
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.valueOf(exception.getHttpStatus());
        final String title = this.resolveOpenAiTitle(exception, status);
        final String details = StringUtils.hasText(exception.getUpstreamMessage())
                ? exception.getUpstreamMessage()
                : "OpenAI request failed";
        return buildError(status, title, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(final ConstraintViolationException exception) {
        final String details = exception.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(exception.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDTO> handleMethodValidation(final Exception exception) {
        if (exception instanceof HandlerMethodValidationException handlerMethodValidationException) {
            final Optional<String> details = handlerMethodValidationException.getAllValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream())
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .findFirst();
            return buildError(HttpStatus.BAD_REQUEST, details.orElse("Validation failed"));
        }
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            final String details = methodArgumentNotValidException.getBindingResult().getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .orElse("Validation failed");
            return buildError(HttpStatus.BAD_REQUEST, details);
        }
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleNotReadable(final HttpMessageNotReadableException exception) {
        return buildError(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    private static ResponseEntity<ErrorDTO> buildError(final HttpStatus status, final String details) {
        return buildError(status, status.getReasonPhrase(), details);
    }

    private static ResponseEntity<ErrorDTO> buildError(final HttpStatus status, final String title, final String details) {
        return ResponseEntity.status(status)
                .body(ErrorDTO.builder()
                        .code(status.value())
                        .title(title)
                        .details(details)
                        .build());
    }

    private String resolveOpenAiTitle(final OpenAiExecutionException exception, final HttpStatus status) {
        if (StringUtils.hasText(exception.getUpstreamType())) {
            return exception.getUpstreamType();
        }
        if (StringUtils.hasText(exception.getUpstreamCode())) {
            return exception.getUpstreamCode();
        }
        return status.getReasonPhrase();
    }
}
