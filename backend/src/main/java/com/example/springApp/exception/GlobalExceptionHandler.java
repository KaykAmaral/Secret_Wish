package com.example.springApp.exception;

import com.example.springApp.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Converte erros de regra de negocio em resposta 400 compreensivel para o cliente.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        LOGGER.warn("Business error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de regra de negocio", ex.getMessage());
    }

    /**
     * Representa conflitos de estado, como email ou participacao ja existentes.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(ConflictException ex) {
        LOGGER.warn("Conflict error: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    /**
     * Padroniza respostas para acesso negado por permissao de dominio.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbiddenException(ForbiddenException ex) {
        LOGGER.warn("Forbidden error: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
    }

    /**
     * Informa ao cliente que uma cota operacional foi excedida.
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitException(RateLimitException ex) {
        LOGGER.warn("Rate limit error: {}", ex.getMessage());
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Limite de uso atingido", ex.getMessage());
    }

    /**
     * Retorna 404 quando a entidade solicitada nao existe ou nao pertence ao usuario.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        LOGGER.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage());
    }

    /**
     * Agrupa erros de validacao por campo para exibicao precisa no frontend.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        LOGGER.warn("Validation error: {}", errors);

        ApiErrorResponse body = buildBody(
                HttpStatus.BAD_REQUEST,
                "Erro de validacao",
                "Existem campos invalidos na requisicao",
                errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata parametros de rota ou query que nao puderam ser convertidos para o tipo esperado.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parametro '%s' possui valor invalido", ex.getName());
        LOGGER.warn("Type mismatch error: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Parametro invalido", message);
    }

    /**
     * Evita vazamento de stack trace ao cliente mantendo o erro registrado no log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        LOGGER.error("Unexpected error", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor",
                "Ocorreu um erro inesperado: " + ex.getMessage()
        );
    }

    /**
     * Cria ResponseEntity com o contrato padrao de erro da API.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String error, String message) {
        return new ResponseEntity<>(buildBody(status, error, message, Collections.emptyMap()), status);
    }

    /**
     * Monta o corpo comum usado por erros simples e erros de validacao por campo.
     */
    private ApiErrorResponse buildBody(
            HttpStatus status,
            String error,
            String message,
            Map<String, String> fields
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                fields
        );
    }

}
