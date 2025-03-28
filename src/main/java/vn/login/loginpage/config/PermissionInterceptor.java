package vn.login.loginpage.config;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import vn.login.loginpage.domain.Role;
import vn.login.loginpage.service.UserService;
import vn.login.loginpage.util.SecurityUtil;
import vn.login.loginpage.util.error.PermissionException;
import reactor.util.function.Tuple2;
import vn.login.loginpage.domain.User;

@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements WebFilter {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    private static final List<String> WHITELIST = List.of(
            "/",
            "/auth/**");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // Check whitelist
        if (isWhitelisted(path)) {
            return chain.filter(exchange); // Skip permission check
        }

        if ("/users".equals(path) && "GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
            return chain.filter(exchange); // ✅ allow GET & POST /users for anyone
        }

        return this.securityUtil.getCurrentUserLoginReactive()
                .flatMap(email -> this.userService.findUserAndRoleByEmail(email)
                        .flatMap((Tuple2<User, Role> tuple) -> {
                            Role role = tuple.getT2();
                            if (role == null || !"ADMIN".equalsIgnoreCase(role.getName())) {
                                return Mono.error(new PermissionException("Access denied: ADMIN role required."));
                            }
                            return chain.filter(exchange);
                        }))
                .switchIfEmpty(chain.filter(exchange)); // Allow anonymous access (or you can block if needed)
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(white -> matchPath(white, path));
    }

    private boolean matchPath(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String base = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(base);
        }
        return path.equals(pattern);
    }
}