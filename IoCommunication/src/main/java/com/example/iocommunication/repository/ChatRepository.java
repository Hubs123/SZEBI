package com.example.iocommunication.repository;
import com.example.iocommunication.model.Chat;
import com.example.iocommunication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByChatName(String chatName);
    List<Chat> findByUsersInChatContains(User user);
}