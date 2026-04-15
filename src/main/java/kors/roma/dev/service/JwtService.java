package kors.roma.dev.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import kors.roma.dev.common.Logger;
import kors.roma.dev.security.JwtSettings;
import kors.roma.dev.security.UserPrincipal;

@Service
public class JwtService {
    private static final long TOKEN_LIFETIME_SECONDS = 600L;

    private final Clock clock;
    private final Logger logger;
    private final String secretKey;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Autowired
    public JwtService(JwtSettings jwtSettings, Logger logger){
        this.secretKey = jwtSettings.getSecret();
        this.logger = logger;
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm).
            withIssuer("myApp").
            build();
        this.clock = Clock.systemUTC();
    }

    public String createJwtToken(UserPrincipal principal){
        List<String> authoritiesList = principal.roles().stream().
            map(role -> {return role.getAuthority();}).toList();
        String[] authorities = authoritiesList.toArray(new String[authoritiesList.size()]);
        String token = JWT.create().
            withIssuer("myApp").
            withSubject(principal.userId().toString()).
            withIssuedAt(Instant.now()).
            withExpiresAt(Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS)).
            withArrayClaim("Authorities", authorities).
            withClaim("userId",principal.userId().toString()).
            sign(algorithm);
        return token;
    }

    /**
     * Method that verifies a JWT
     * @param jwtString - the String representation of the token
     * @return an Optional that is empty if the token is invalid
     */
    public Optional<DecodedJWT> verifyJwt(String jwtString){
        try{
            DecodedJWT decodedJwt = JWT.decode(jwtString);
            if(decodedJwt.getExpiresAtAsInstant().isBefore(clock.instant())){
                return Optional.empty();
            }
            decodedJwt = verifier.verify(decodedJwt);
            return Optional.of(decodedJwt);
        } catch(JWTVerificationException ex){
            logger.logErr(ex.getMessage(),ex);
            return Optional.empty();
        }
    }
}
