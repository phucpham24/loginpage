package vn.login.loginpage.util.error;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.response.ResResponse;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = {
            InvalidException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handleInvalidException(InvalidException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Exception occurs ....");

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res));
    }

    @ExceptionHandler(value = {
            BadCredentialsException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handleBadCredentialException(BadCredentialsException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError(ex.getMessage());
        res.setMessage("Invalid username or password");

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res));
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handleUserNotFoundException(UsernameNotFoundException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(ex.getMessage());
        res.setMessage("UserName not found ....");

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(res));
    }

    @ExceptionHandler(value = {
            PermissionException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handelPermissionException(PermissionException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError(ex.getMessage());
        res.setMessage("Forbidden ....");

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(res));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResResponse<Object>>> handleGenericException(Exception ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(ex.getMessage());
        res.setMessage("Internal server error");

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res));
    }
}
