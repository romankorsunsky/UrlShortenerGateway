package kors.roma.dev.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kors.roma.dev.model.User;
import kors.roma.dev.repository.UserRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    
    private final UserRepository userRepo;
    
    public UserDetailsServiceImpl(UserRepository userRepo)
    {
        this.userRepo = userRepo;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        var usr = userRepo.findByUsername(username).
            orElseThrow(()->{return new UsernameNotFoundException("No User Bro");});
        return usr;
    }
}
