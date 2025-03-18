package vn.login.loginpage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = false)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        String[] whiteList = {
                "/",
                "/api/v1/user/",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh"
        };

        http
                .csrf(c -> c.disable())
                .authorizeHttpRequests(
                        authz -> authz
                                // .requestMatchers(whiteList).permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/jobs/**").permitAll()
                                // .requestMatchers(HttpMethod.GET, "/api/v1/skills/**").permitAll()
                                .anyRequest().permitAll())
                // .anyRequest().permitAll())
                // .exceptionHandling(
                // exceptions -> exceptions
                // .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 401
                // .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) // 403

                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
