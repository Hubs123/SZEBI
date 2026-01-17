package com.example.iocommunication.repository;

import com.example.iocommunication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByFirstNameStartingWith(String prefix);
    Optional<User> findByUsername(String username);
}