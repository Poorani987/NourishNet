package com.nourishnet.repository;

import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(UserRole role);
    long count();
    void deleteById(String id);
}
