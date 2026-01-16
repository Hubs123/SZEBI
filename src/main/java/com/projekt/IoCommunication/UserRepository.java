package com.projekt.IoCommunication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByFirstNameStartingWith(String prefix);
    User findByUsername(String username);
}