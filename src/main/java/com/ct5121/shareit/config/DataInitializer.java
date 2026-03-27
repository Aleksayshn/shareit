package com.ct5121.shareit.config;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.service.BookingService;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.item.service.ItemService;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponseDto;
import com.ct5121.shareit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("demo")
public class DataInitializer {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Restrict demo seed data to dedicated profile to avoid side effects in prod/test runs.
            String suffix = String.valueOf(System.currentTimeMillis());

            UserRequestDto ownerRequest = new UserRequestDto();
            ownerRequest.setName("Alex");
            ownerRequest.setEmail("alex+" + suffix + "@test.com");

            UserRequestDto bookerRequest = new UserRequestDto();
            bookerRequest.setName("Mary");
            bookerRequest.setEmail("mary+" + suffix + "@test.com");

            UserResponseDto owner = userService.addUser(ownerRequest);
            UserResponseDto booker = userService.addUser(bookerRequest);

            ItemResponseDto item = itemService.addItem(
                    owner.getId(),
                    new ItemRequestDto("Drill", "Power drill", true)
            );

            BookingRequestDto bookingRequest = new BookingRequestDto();
            bookingRequest.setItemId(item.getId());
            bookingRequest.setStart(LocalDateTime.now().plusDays(1));
            bookingRequest.setEnd(LocalDateTime.now().plusDays(2));

            BookingResponseDto created = bookingService.createBooking(booker.getId(), bookingRequest);
            BookingResponseDto approved = bookingService.approveBooking(owner.getId(), created.getId(), true);
            BookingResponseDto fetched = bookingService.getBooking(booker.getId(), approved.getId());

            log.info("Demo completed: owner={}, booker={}, item={}, booking={}, status={}",
                    owner.getId(),
                    booker.getId(),
                    item.getId(),
                    fetched.getId(),
                    fetched.getStatus());
        };
    }
}
