package kors.roma.dev.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import kors.roma.dev.model.User;


public interface UserRepository extends CrudRepository<User, UUID>{
    @Query("select u from User u where u.username = :usrname")
    public Optional<User> findByUsername(@Param("usrname") String username);

    public void deleteByUsername(String username);
}
