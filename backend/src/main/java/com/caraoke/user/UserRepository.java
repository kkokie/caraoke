package com.caraoke.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthUid(String authUid);
    Optional<User> findByHandle(String handle);
    boolean existsByHandle(String handle);
}
