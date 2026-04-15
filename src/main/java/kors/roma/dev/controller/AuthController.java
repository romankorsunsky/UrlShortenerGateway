package kors.roma.dev.controller;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import kors.roma.dev.common.Logger;
import kors.roma.dev.dto.request.AuthRequest;
import kors.roma.dev.dto.request.RegistrationRequest;
import kors.roma.dev.dto.response.AuthResponse;
import kors.roma.dev.security.UserPrincipal;
import kors.roma.dev.service.AuthenticationLifecycleService;


@RestController
@RequestMapping("api")
public class AuthController {
    
    private final AuthenticationLifecycleService authService;
    private final Logger logger;

    public AuthController(Logger logger, AuthenticationLifecycleService authService){
        this.logger = logger;
        this.authService = authService;
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest)
    {   
        if(authRequest.username() == null || authRequest.password() == null ||
            authRequest.username().isBlank() || authRequest.password().isBlank())
        {
            return ResponseEntity.badRequest().build();
        }
        var token = authService.authenticate(authRequest);
        if(token.isEmpty()){
            return ResponseEntity.status(Response.SC_UNAUTHORIZED).build();
        }
        AuthResponse response = new AuthResponse(token.get());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/users")
    public ResponseEntity<Object> register(@RequestBody RegistrationRequest regForm) {
        try {
            boolean createdUser = authService.registerUser(regForm);
            if(!createdUser){
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).build();
            }
            return ResponseEntity.created(null).build();
        } catch (Exception e) {
            // should set the created URI, but I am not sure what to return exactly.
            // it doesn't make sense to return https://<domain_name>/api/users/<created_user_id>
            // because the user doesn't need it. It would make sense to return:
            // https://<domain_name>/api/users/<created_user_id>/collected_data or something like that
            return ResponseEntity.internalServerError().build();
        }
    }

    @RequestMapping(path="/delete", method=RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser() {
        try {
            var principal = (UserPrincipal) SecurityContextHolder.getContext().
                getAuthentication().getPrincipal();
            authService.deleteUser(principal);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        catch(NullPointerException | ClassCastException e) {
            logger.logErr("Couldn't obtain principal", e);
            throw e;
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(null);
        }
    }
    
}
