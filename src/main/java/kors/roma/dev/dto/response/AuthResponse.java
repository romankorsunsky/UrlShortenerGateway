package kors.roma.dev.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter // important to have this, or else Jackson/GSON won;t be able to acces the token
        // when serializing to network.
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
