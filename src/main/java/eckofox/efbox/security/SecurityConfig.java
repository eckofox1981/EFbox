package eckofox.efbox.security;

import eckofox.efbox.user.GrantedAuthorities;
import eckofox.efbox.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * checks which endpoints need security filter, only account creation and login are public. All other functions
     * require authentication. Ownership of files and folders is checked in various service methods and sometimes
     * even in repository methods.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf((AbstractHttpConfigurer::disable))
                .authorizeHttpRequests
                        (auth -> auth
                                .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user/login").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user/password-recovery").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user/change-password").permitAll()
                                .requestMatchers(
                                        HttpMethod.PUT, "/bossmang/grant-admin-status").hasRole("OWNER")
                                .requestMatchers(HttpMethod.PUT, "/bossmang/grant-log-access").hasRole("ADMIN")
                                .requestMatchers(HttpMethod
                                        .PUT, "/bossmang/request-log-access").hasRole("ADMIN")
                                .requestMatchers(HttpMethod
                                        .GET, "/bossmang/fetch-all-logs")
                                        .hasAuthority(GrantedAuthorities.LOG_ACCESS.toString())
                                .requestMatchers(HttpMethod
                                        .DELETE, "/bossmang/revoke-admin-status")
                                        .hasAuthority(GrantedAuthorities.REVOKE_ADMIN_ROLE.toString())
                                .requestMatchers(HttpMethod.DELETE, "/bossmang/revoke-log-access")
                                        .hasAuthority(GrantedAuthorities.REVOKE_LOG_ACCESS.toString())
                                .anyRequest().authenticated()
                        )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(new JWTFilter(userService), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    /** https://www.baeldung.com/spring-cors
     * https://www.youngju.dev/blog/architecture/2026-03-08-sso-cookie-jwt-auth-spring-boot.en#cors--credential-transmission-setup
     * @return
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost",
                "http://127.0.0.1"
        ));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("Content-Type", "X-Requested-With", "Accept"));
        corsConfiguration.setExposedHeaders(List.of("Set-Cookie"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(60L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
