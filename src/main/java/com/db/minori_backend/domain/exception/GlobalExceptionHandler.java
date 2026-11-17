package com.db.minori_backend.domain.exception;

import com.db.minori_backend.domain.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> recursoNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> erroValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        campos.putIfAbsent(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Existem campos inválidos na requisição.",
                request,
                campos
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está inválido ou malformado.",
                request,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> violacaoIntegridade(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado.",
                request,
                null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> argumentoInvalido(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return criarResposta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null
        );
    }

    private ResponseEntity<ErroResponse> criarResposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );

        return ResponseEntity.status(status).body(erro);
    }
}