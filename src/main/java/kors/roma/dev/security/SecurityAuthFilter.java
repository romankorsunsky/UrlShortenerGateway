package kors.roma.dev.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.Claim;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kors.roma.dev.common.Logger;
import kors.roma.dev.service.JwtService;

@Component
public class SecurityAuthFilter extends OncePerRequestFilter {
    private final Logger logger;
    private final JwtService jwtService;
    
    @Autowired
    public SecurityAuthFilter(JwtService jwtService, Logger logger){
        this.jwtService = jwtService;
        this.logger = logger;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException
    {
        var authHeaderValue = request.getHeader("Authorization");
        if(authHeaderValue == null){
            filterChain.doFilter(request, response);
            return;
        }
        var tokenContainer = jwtService.verifyJwt(authHeaderValue.substring(7));
        if(tokenContainer.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }
        var token = tokenContainer.get();
        
        var id = UUID.fromString(token.getSubject());
        Claim authClaims = token.getClaim("Authorities");
        List<Role> roles = null;
        if(authClaims != null){
            roles = authClaims.asList(String.class).stream().map(Role::new).toList();
        }
        
        UserPrincipal principal = new UserPrincipal(id, roles);
        Authentication authentication = 
            new UsernamePasswordAuthenticationToken(principal,null,principal.roles());

        SecurityContext secCtx = SecurityContextHolder.createEmptyContext();
        secCtx.setAuthentication(authentication);
        SecurityContextHolder.setContext(secCtx);
        filterChain.doFilter(request, response);
    }
}
