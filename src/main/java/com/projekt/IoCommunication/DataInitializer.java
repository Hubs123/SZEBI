package com.projekt.IoCommunication;

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
            if (userRepository.count() == 0){
                User oldPiotr = userRepository.findByUsername("piotr.nowak");
                if (oldPiotr != null) {
                    userRepository.delete(oldPiotr);
                    System.out.println("Usunięto starego użytkownika: piotr.nowak");
                }

                if (userRepository.count() == 0) {
                    User adminPiotr = new User();
                    adminPiotr.setFirstName("Piotr");
                    adminPiotr.setLastName("Nowak");
                    adminPiotr.setUsername("piotr.nowak");
                    adminPiotr.setRole("ADMIN");
                    adminPiotr.setLastLogin(new Date());
                    userRepository.save(adminPiotr);

                    System.out.println("Dodano użytkownika ADMIN: Piotr Nowak");

                    User userAnna = new User();
                    userAnna.setFirstName("Anna");
                    userAnna.setLastName("Nowak");
                    userAnna.setUsername("anna.nowak");
                    userAnna.setRole("USER");
                    userAnna.setLastLogin(new Date());
                    userRepository.save(userAnna);

                    System.out.println("Dodano użytkownika USER: Anna Nowak");

                    Chat sampleChat = new Chat();
                    sampleChat.setChatName("Czat testowy");

                    sampleChat.addUser(adminPiotr);
                    sampleChat.addUser(userAnna);

                    chatRepository.save(sampleChat);

                    System.out.println("Dodano przykładowy czat: " + sampleChat.getChatName());
                    Message welcomeMsg = new Message();
                    welcomeMsg.setSender(adminPiotr);
                    welcomeMsg.setContent("Witaj w czacie testowym!");
                    welcomeMsg.setDateCreated(new Date());

                    List<Message> messages = new ArrayList<>();
                    messages.add(welcomeMsg);

                    sampleChat.setMessages(messages);
                    chatRepository.save(sampleChat);

                    System.out.println("Dodano przykładową wiadomość do czatu testowego.");
                }
            }
        };
    }
}
