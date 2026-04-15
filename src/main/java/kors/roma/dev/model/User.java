package kors.roma.dev.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import kors.roma.dev.security.IdentifiableUser;
import kors.roma.dev.security.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access=AccessLevel.PRIVATE,force=true)
@Entity
@Table(name = "users")
@BatchSize(size=50)
public class User implements IdentifiableUser, Persistable<UUID>{
    
    @Id
    @Column(name="id")
    private UUID uid;

    private String username;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="password")
    private String password;

    @Column(name="email")
    private String email;
    
    @Column(name="created_at")
    private Instant created_at = Instant.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="users_roles",
        joinColumns = {@JoinColumn(name = "user_id")},
        inverseJoinColumns = {@JoinColumn(name = "role_id")}
    )
    private List<Role> roles;

    @Transient
    private boolean isNew = true;

    public User(UUID id,String username,String firstName,String lastName,
        String password, String email) throws NullPointerException
    {
        if(id == null || username == null || firstName == null ||
            lastName == null || password == null || email == null)
        {
            throw new NullPointerException("User can't have null fields");
        }
        this.uid = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.roles = new ArrayList<>();
    }

    @Override
    public List<Role> getAuthorities() {
        return this.roles;
    }
    
    public void addRole(Role role){
        this.roles.add(role);
    }

    public void removeRole(Role role) throws NullPointerException{
        if(role == null){
            throw new NullPointerException("Role cannot be null");
        }
        if(this.roles.contains(role)){
            roles.remove(role);
        }
    }

    @Override
    public @Nullable UUID getId() {
        return this.uid;
    }

    @PrePersist
    @PostLoad
    void turnNotNew(){
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
