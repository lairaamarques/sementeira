package br.edu.ifam.sementeira.exception;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraNegocioException(String mensagem) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }

    public RegraNegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static RegraNegocioException naoEncontrado(String oQue) {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, oQue + " não encontrado(a)");
    }
}
