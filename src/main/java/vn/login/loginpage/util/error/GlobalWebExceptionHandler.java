package vn.login.loginpage.util.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.response.ResResponse;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal server error";

        if (ex instanceof PermissionException) {
            status = HttpStatus.FORBIDDEN;
            message = ex.getMessage();
        } else if (ex instanceof InvalidException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        }

        ResResponse<Object> body = new ResResponse<>();
        body.setStatusCode(status.value());
        body.setError(ex.getMessage());
        body.setMessage(message);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(body);
            return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(responseBytes)));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}
