package kors.roma.dev.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

public interface IdentifiableUser extends UserDetails{
    public UUID getUid();
}
