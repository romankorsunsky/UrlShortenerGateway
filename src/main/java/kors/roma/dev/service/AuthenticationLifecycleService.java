package kors.roma.dev.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.uuid.Generators;

import kors.roma.dev.common.Logger;
import kors.roma.dev.common.RabbitUserEventPublisher;
import kors.roma.dev.common.UserPublishAdapter;
import kors.roma.dev.dto.request.AuthRequest;
import kors.roma.dev.dto.request.RegistrationRequest;
import kors.roma.dev.exceptions.UserNotFoundException;
import kors.roma.dev.model.User;
import kors.roma.dev.repository.RoleRepository;
import kors.roma.dev.repository.UserRepository;
import kors.roma.dev.security.Role;
import kors.roma.dev.security.UserPrincipal;

@Service
public class AuthenticationLifecycleService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final Logger logger;
    private final PasswordEncoder pwdEncoder;
    private final UserPublishAdapter<RabbitUserEventPublisher> userPublishAdapter;
    

    public AuthenticationLifecycleService(AuthenticationManager manager, JwtService jwtService,
        UserRepository userRepo, RoleRepository roleRepo,
        Logger logger,PasswordEncoder encoder,
        UserPublishAdapter<RabbitUserEventPublisher> adapter)
    {
        this.authManager = manager;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.logger = logger;
        this.pwdEncoder = encoder;
        this.userPublishAdapter = adapter;
    }

    public Optional<String> authenticate(AuthRequest authRequest){
        String username = authRequest.username();
        String password = authRequest.password();

        UsernamePasswordAuthenticationToken usrPwdReq = 
            new UsernamePasswordAuthenticationToken(username, password);
        try {
            var principal = (UserPrincipal)authManager.authenticate(usrPwdReq).getPrincipal();
            var token = jwtService.createJwtToken(principal);
            return Optional.of(token);
        } 
        catch (AuthenticationException e) {
            return Optional.empty();
        }
    }

    /**
     * Tries to create a User and persist.
     * @param form - RegustrationForm
     * @return true on sucess, false otherwise.
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
    )
    public boolean registerUser(RegistrationRequest form) throws Exception
    {
        if(!RegistrationRequest.isValid(form)){
            logger.logInfo("registration form not valid");
            return false;
        }
        var username = form.username();
        var firstName = form.firstName();
        var lastName = form.lastName();
        var email = form.email();
        var passwordHash = pwdEncoder.encode(form.password());
        var id = Generators.timeBasedEpochGenerator().generate();
        var usr = new User(id,username,firstName,lastName,passwordHash,email);
        
        Optional<Role> role = roleRepo.findByName("ROLE_USER");
        if(!role.isPresent()){
            return false;
        }
        usr.addRole(role.get());
        userRepo.save(usr);
        try {
            userPublishAdapter.publishUserCreated(usr);
        } catch (Exception e) { 
            userRepo.delete(usr);
            throw e; //actually, need to think what to do on failure
        }
        return true;
    }

    
    @Transactional
    public void deleteUser(UserPrincipal principal) throws NullPointerException,
        UserNotFoundException,Exception
    {
        if(principal == null){
            throw new NullPointerException();
        }
        var uuid = principal.userId();
        try {
            var user = userRepo.findById(uuid);
            if(!user.isPresent()){
                throw new UserNotFoundException(uuid.toString());
            }
            userRepo.deleteById(uuid);
            userPublishAdapter.publishUserDeleted(user.get());
        }
        catch (IllegalArgumentException e) {
            logger.logErr("Could not delete User ", e);
            throw e;
        } 
        catch (Exception e) { 
            throw e;
        }
    }
}
