package kors.roma.dev.repository;

import org.springframework.data.repository.CrudRepository;

import kors.roma.dev.security.Role;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role,Integer>{
    public Optional<Role> findByName(String name);
}
