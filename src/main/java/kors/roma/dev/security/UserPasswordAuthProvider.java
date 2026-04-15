package kors.roma.dev.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserPasswordAuthProvider implements AuthenticationProvider{

    //private AuthenticationManager authManager;
    private final UserDetailsService uds;
    private final PasswordEncoder encoder;

    public UserPasswordAuthProvider(UserDetailsService uds, PasswordEncoder encoder){
        //this.authManager = manager;
        this.uds = uds;
        this.encoder = encoder;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var user = uds.loadUserByUsername(authentication.getName());

        if(!(authentication instanceof UsernamePasswordAuthenticationToken auth)){
            throw new InternalAuthenticationServiceException(
                "authentication not of type UsernamePasswordAuthenticationToken");
        }
        var password = auth.getCredentials().toString();

        if(!encoder.matches(password,user.getPassword())){
            throw new BadCredentialsException(authentication.getName());
        }
        
        var principal = UserToPrincipalMapper.map((IdentifiableUser)user);
        return new UsernamePasswordAuthenticationToken(principal, null,principal.roles());    
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
}
