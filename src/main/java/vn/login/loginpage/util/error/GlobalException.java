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
            BadCredentialsException.class,
            InvalidException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handleBadCredentialException(InvalidException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Exception occurs ....");

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res));
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class
    })
    public Mono<ResponseEntity<ResResponse<Object>>> handleUserNotFoundException(InvalidException ex) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("UserName not found ....");

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res));
    }

    // @ExceptionHandler(value = {
    // PermissionException.class
    // })
    // public Mono<ResponseEntity<ResResponse<Object>>>
    // handelPermissionException(InvalidException ex) {
    // ResResponse<Object> res = new ResResponse<>();
    // res.setStatusCode(HttpStatus.BAD_REQUEST.value());
    // res.setError(ex.getMessage());
    // res.setMessage("Forbidden ....");

    // return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(res));
    // }
}
