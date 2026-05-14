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
                requestMatchers(HttpMethod.POST,"/api/users").permitAll()). //register
            authorizeHttpRequests(auth -> auth.
                requestMatchers(HttpMethod.DELETE, "/api/user").authenticated()). //delete
                authorizeHttpRequests(auth -> auth.
                    requestMatchers(HttpMethod.PATCH, "/api/user").authenticated()). //update password
            authorizeHttpRequests(auth -> auth.
                requestMatchers("/api/auth/login").permitAll()). //login
            authorizeHttpRequests(auth -> auth.
                anyRequest().authenticated()). //best practice is to restrict EVERYYING and relax as I go
            csrf(customizer -> customizer.disable());
        return httpsec.build();
    }
}
