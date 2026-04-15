package kors.roma.dev.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecConfig {

    @Autowired
    private AuthenticationProvider authProvider;
    
    @Autowired
    private SecurityAuthFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http){
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(authProvider);
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpsec) throws Exception{
        httpsec.
            addFilterBefore(jwtFilter, AuthorizationFilter.class).
            authorizeHttpRequests(auth -> auth.
                requestMatchers("/api/auth/delete/*").authenticated()).
            authorizeHttpRequests(auth -> auth.
                requestMatchers("/api/auth/register","/api/auth/login").permitAll()).
            authorizeHttpRequests(auth -> auth.
                requestMatchers(HttpMethod.POST,"/api/uris/longuri").authenticated()).
            authorizeHttpRequests(auth -> auth.
                anyRequest().permitAll()).
            csrf(customizer -> customizer.disable());
        return httpsec.build();
    }
}
