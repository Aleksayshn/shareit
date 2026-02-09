package com.ct5121.shareit.config;

import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.item.repository.ItemRepository;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

 @Bean
 CommandLineRunner initData() {
     return args -> {
         itemRepository.deleteAll();
         userRepository.deleteAll();

         User u1 = new User();
         u1.setName("Alex");
         u1.setEmail("alex@test.com");

         User u2 = new User();
         u2.setName("Mary");
         u2.setEmail("mary@test.com");

         u1 = userRepository.save(u1);
         u2 = userRepository.save(u2);

         Item i1 = new Item(null, "Drill", "Power drill", true, u1, null);
         Item i2 = new Item(null, "Bike", "Mountain bike", true, u1, 100L);
         Item i3 = new Item(null, "Camera", "DSLR", false, u2, null);

         itemRepository.save(i1);
         itemRepository.save(i2);
         itemRepository.save(i3);
     };
 }
}
