package vn.login.loginpage.util.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.login.loginpage.domain.response.ResResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

public class ResponseWrapper {

    public static <T> Mono<ResponseEntity<ResResponse<T>>> wrapMono(Mono<T> mono, String message, HttpStatus status) {
        return mono.map(data -> {
            ResResponse<T> response = new ResResponse<>();
            response.setStatusCode(status.value());
            response.setMessage(message);
            response.setData(data);
            response.setError(null);
            return ResponseEntity.status(status).body(response);
        }).defaultIfEmpty(ResponseEntity.status(status).body(new ResResponse<T>() {
            {
                setStatusCode(status.value());
                setMessage(message);
                setData(null);
                setError(null);
            }
        }));
    }

    public static <T> Mono<ResponseEntity<ResResponse<List<T>>>> wrapFlux(Flux<T> flux, String message,
            HttpStatus status) {
        return flux.collectList().map(list -> {
            ResResponse<List<T>> response = new ResResponse<>();
            response.setStatusCode(status.value());
            response.setMessage(message);
            response.setData(list);
            response.setError(null);
            return ResponseEntity.status(status).body(response);
        });
    }
}
