package com.ct5121.shareit.config;

import com.ct5121.shareit.booking.dto.BookingRequestDto;
import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.service.BookingService;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponesDto;
import com.ct5121.shareit.item.service.ItemService;
import com.ct5121.shareit.user.dto.UserRequestDto;
import com.ct5121.shareit.user.dto.UserResponesDto;
import com.ct5121.shareit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            String suffix = String.valueOf(System.currentTimeMillis());

            UserRequestDto ownerRequest = new UserRequestDto();
            ownerRequest.setName("Alex");
            ownerRequest.setEmail("alex+" + suffix + "@test.com");

            UserRequestDto bookerRequest = new UserRequestDto();
            bookerRequest.setName("Mary");
            bookerRequest.setEmail("mary+" + suffix + "@test.com");

            UserResponesDto owner = userService.addUser(ownerRequest);
            UserResponesDto booker = userService.addUser(bookerRequest);

            ItemResponesDto item = itemService.addItem(
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

            System.out.println("Demo completed:");
            System.out.println("Owner ID = " + owner.getId() + ", Booker ID = " + booker.getId());
            System.out.println("Item ID = " + item.getId());
            System.out.println("Booking ID = " + fetched.getId() + ", Status = " + fetched.getStatus());
        };
    }
}
