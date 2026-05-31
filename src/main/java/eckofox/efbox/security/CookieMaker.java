package eckofox.efbox.security;

import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Configuration;

/**
 * Cookie handling class'
 * based on https://codingtechroom.com/question/insert-cookies-in-rest-response-spring
 */

@Configuration
public class CookieMaker {
    public Cookie cookieBaker(String token) {
        Cookie cookie = new Cookie("efbox-token", token);
        cookie.setPath("/");
        //cookie.setDomain(System.getenv("DOMAIN_BASEURL")); removed for local development purposes
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(600);

        return cookie;
    }
}
