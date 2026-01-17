package com.example.iocommunication.seed;

import com.example.iocommunication.model.Message;
import com.example.iocommunication.model.User;
import com.example.iocommunication.model.Chat;
import com.example.iocommunication.repository.ChatRepository;
import com.example.iocommunication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      ChatRepository chatRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {

                Optional<User> oldPiotr = userRepository.findByUsername("piotr.nowak");
                oldPiotr.ifPresent(userRepository::delete);


                User adminPiotr = new User();
                adminPiotr.setFirstName("Piotr");
                adminPiotr.setLastName("Nowak");
                adminPiotr.setUsername("piotr.nowak");
                adminPiotr.setRole("ROLE_ADMIN");
                adminPiotr.setPassword(passwordEncoder.encode("admin123"));
                adminPiotr.setLastLogin(new Date());
                userRepository.save(adminPiotr);
                System.out.println("Dodano użytkownika ADMIN: Piotr Nowak");


                User userAnna = new User();
                userAnna.setFirstName("Anna");
                userAnna.setLastName("Nowak");
                userAnna.setUsername("anna.nowak");
                userAnna.setRole("ROLE_USER");
                userAnna.setPassword(passwordEncoder.encode("user123"));
                userAnna.setLastLogin(new Date());
                userRepository.save(userAnna);
                System.out.println("Dodano użytkownika USER: Anna Nowak");

                List<User> allUsers = List.of(adminPiotr, userAnna);

                for (User u : allUsers) {
                    Chat chat = new Chat();
                    chat.setChatName("Czat testowy dla " + u.getFirstName());
                    chat.addUser(u);

                    Message welcomeMsg = new Message();
                    welcomeMsg.setSender(u);
                    welcomeMsg.setContent("Witaj w czacie testowym, " + u.getFirstName() + "!");
                    welcomeMsg.setDateCreated(new Date());

                    List<Message> messages = new ArrayList<>();
                    messages.add(welcomeMsg);
                    chat.setMessages(messages);

                    chatRepository.save(chat);
                    System.out.println("Dodano czat początkowy dla użytkownika: " + u.getUsername());
                }


                Chat wspolnyChat = new Chat();
                wspolnyChat.setChatName("Czat testowy wspólny");
                allUsers.forEach(wspolnyChat::addUser);

                Message welcomeShared = new Message();
                welcomeShared.setSender(adminPiotr);
                welcomeShared.setContent("Witajcie w wspólnym czacie testowym!");
                welcomeShared.setDateCreated(new Date());


                List<Message> sharedMessages = new ArrayList<>();
                sharedMessages.add(welcomeShared);
                wspolnyChat.setMessages(sharedMessages);

                chatRepository.save(wspolnyChat);
                System.out.println("Dodano wspólny czat testowy dla wszystkich użytkowników.");
            }
        };
    }
}
