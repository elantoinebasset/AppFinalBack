package com.timescheduler.repository;

import com.timescheduler.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return find("username", username).firstResult() != null;
    }

    public boolean existsByEmail(String email) {
        return find("email", email).firstResult() != null;
    }
}
