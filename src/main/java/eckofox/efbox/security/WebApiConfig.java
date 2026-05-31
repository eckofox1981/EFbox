package eckofox.efbox.security;

import eckofox.efbox.security.ratelimiting.RateLimitingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * used to insert the RateLimitingInterceptor before each request
 */
@Configuration
@RequiredArgsConstructor
public class WebApiConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/bossmang/**");
    }
}
