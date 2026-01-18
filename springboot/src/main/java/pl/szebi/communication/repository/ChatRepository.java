package pl.szebi.communication.repository;
import pl.szebi.communication.model.Chat;
import pl.szebi.communication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByChatName(String chatName);
    List<Chat> findByUsersInChatContains(User user);
}