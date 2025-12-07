package com.example.iocommunication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class DataInitializer {
    //Ta klasa jest do tworzenia poczatkowych danych wiec w sumie juz mozna ja wywalic bo dane sa w bazie
    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      ChatRepository chatRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User testUser = new User();
                testUser.setFirstName("Piotr");
                testUser.setLastName("Nowak");
                testUser.setUsername("piotr.nowak");
                testUser.setRole("USER");
                testUser.setLastLogin(new Date());
                userRepository.save(testUser);

                System.out.println("Dodano testowego użytkownika: Piotr Nowak");

                Chat sampleChat = new Chat();
                sampleChat.setChatName("Czat testowy");
                List<User> participants = new ArrayList<>();
                participants.add(testUser);
                sampleChat.addUser(userRepository.findByUsername("piotr.nowak"));
                chatRepository.save(sampleChat);

                System.out.println("Dodano przykładowy czat: " + sampleChat.getChatName());

                Message welcomeMsg = new Message();
                welcomeMsg.setSender(testUser);
                welcomeMsg.setContent("Witaj w czacie testowym!");
                welcomeMsg.setDateCreated(new Date());
                List<Message> messages = new ArrayList<>();
                messages.add(welcomeMsg);
                sampleChat.setMessages(messages);
                chatRepository.save(sampleChat);

                System.out.println("Dodano przykładową wiadomość do czatu testowego.");
            }
        };
    }
}
