package kors.roma.dev.security;

import org.springframework.security.core.AuthenticationException;
public class UserToPrincipalMapper {
    public static UserPrincipal map(IdentifiableUser user) throws AuthenticationException{
        
        return new UserPrincipal(user.getUid(),user.getAuthorities());
    }
}
