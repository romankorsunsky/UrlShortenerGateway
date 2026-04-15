package kors.roma.dev.security;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

public record UserPrincipal(UUID userId,Collection<? extends GrantedAuthority> roles)
    implements Principal
{

	@Override
	public String getName() {
		return userId.toString();
	}}
