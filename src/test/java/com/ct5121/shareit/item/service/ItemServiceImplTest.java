package com.ct5121.shareit.item.service;

import com.ct5121.shareit.booking.dto.BookingResponseDto;
import com.ct5121.shareit.booking.mapper.BookingMapper;
import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.booking.repository.BookingRepository;
import com.ct5121.shareit.comment.dto.CommentDto;
import com.ct5121.shareit.comment.dto.CommentResponseDto;
import com.ct5121.shareit.comment.mapper.CommentMapper;
import com.ct5121.shareit.comment.model.Comment;
import com.ct5121.shareit.comment.repository.CommentRepository;
import com.ct5121.shareit.exception.BadRequestException;
import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.item.mapper.ItemMapper;
import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.item.repository.ItemRepository;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ct5121.shareit.support.TestDataFactory.booking;
import static com.ct5121.shareit.support.TestDataFactory.bookingResponseDto;
import static com.ct5121.shareit.support.TestDataFactory.comment;
import static com.ct5121.shareit.support.TestDataFactory.commentDto;
import static com.ct5121.shareit.support.TestDataFactory.commentResponseDto;
import static com.ct5121.shareit.support.TestDataFactory.item;
import static com.ct5121.shareit.support.TestDataFactory.itemRequestDto;
import static com.ct5121.shareit.support.TestDataFactory.itemResponseDto;
import static com.ct5121.shareit.support.TestDataFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private BookingMapper bookingMapper;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(
                userRepository,
                itemRepository,
                bookingRepository,
                commentRepository,
                itemMapper,
                commentMapper,
                bookingMapper);
    }

    @AfterEach
    void clearState() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddItemForExistingUser() {
        User owner = user(1L, "Owner", "owner@example.com");
        ItemRequestDto request = itemRequestDto("Drill", "Powerful drill", true);
        Item mappedItem = new Item(null, "Drill", "Powerful drill", true, null, null);
        Item savedItem = new Item(10L, "Drill", "Powerful drill", true, owner, null);
        ItemResponseDto response = itemResponseDto(10L, "Drill", true, owner.getId());

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(request)).thenReturn(mappedItem);
        when(itemRepository.save(mappedItem)).thenReturn(savedItem);
        when(itemMapper.toItemResponse(savedItem)).thenReturn(response);

        ItemResponseDto actualResponse = itemService.addItem(1L, request);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(mappedItem.getOwner()).isEqualTo(owner);
    }

    @Test
    void shouldThrowWhenAddingItemForMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(1L, itemRequestDto("Drill", "Powerful drill", true)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 1 not found");

        verifyNoInteractions(itemMapper, itemRepository);
    }

    @Test
    void shouldUpdateItemForOwner() {
        User owner = user(1L, "Owner", "owner@example.com");
        Item existingItem = item(10L, "Drill", true, owner);
        ItemRequestDto request = itemRequestDto("Updated Drill", "Updated description", false);
        ItemResponseDto response = itemResponseDto(10L, "Updated Drill", false, owner.getId());

        when(itemRepository.findById(10L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);
        when(itemMapper.toItemResponse(existingItem)).thenReturn(response);

        ItemResponseDto actualResponse = itemService.updateItem(1L, 10L, request);

        assertThat(actualResponse).isEqualTo(response);
        assertThat(existingItem.getName()).isEqualTo("Updated Drill");
        assertThat(existingItem.getDescription()).isEqualTo("Updated description");
        assertThat(existingItem.isAvailable()).isFalse();
    }

    @Test
    void shouldThrowWhenUpdatingItemByNonOwner() {
        User owner = user(1L, "Owner", "owner@example.com");
        Item existingItem = item(10L, "Drill", true, owner);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> itemService.updateItem(2L, 10L, itemRequestDto("Updated", "Updated", true)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User is not the owner of this item");

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldGetItemWithCommentsAndBookingSummariesForOwner() {
        User owner = user(1L, "Owner", "owner@example.com");
        User booker = user(2L, "Booker", "booker@example.com");
        Item item = item(10L, "Drill", true, owner);
        Comment comment = comment(100L, "Useful item", item, booker, LocalDateTime.of(2026, 4, 1, 10, 0));
        CommentResponseDto commentResponse = commentResponseDto(
                100L,
                "Useful item",
                "Booker",
                LocalDateTime.of(2026, 4, 1, 10, 0));
        Booking lastBooking = booking(
                11L,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3),
                item,
                booker,
                Booking.BookingStatus.APPROVED);
        Booking nextBooking = booking(
                12L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                item,
                booker,
                Booking.BookingStatus.APPROVED);
        BookingResponseDto lastBookingResponse = bookingResponseDto(
                11L,
                lastBooking.getStart(),
                lastBooking.getEnd(),
                Booking.BookingStatus.APPROVED);
        BookingResponseDto nextBookingResponse = bookingResponseDto(
                12L,
                nextBooking.getStart(),
                nextBooking.getEnd(),
                Booking.BookingStatus.APPROVED);
        ItemResponseDto response = itemResponseDto(10L, "Drill", true, owner.getId());

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(response);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentResponseDto(comment)).thenReturn(commentResponse);
        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                eq(10L),
                eq(Booking.BookingStatus.APPROVED),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                eq(10L),
                eq(Booking.BookingStatus.APPROVED),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));
        when(bookingMapper.toBookingResponseDto(lastBooking)).thenReturn(lastBookingResponse);
        when(bookingMapper.toBookingResponseDto(nextBooking)).thenReturn(nextBookingResponse);

        ItemResponseDto actualResponse = itemService.getItem(1L, 10L);

        assertThat(actualResponse.getComments()).containsExactly(commentResponse);
        assertThat(actualResponse.getLastBooking()).isEqualTo(lastBookingResponse);
        assertThat(actualResponse.getNextBooking()).isEqualTo(nextBookingResponse);
    }

    @Test
    void shouldGetItemWithoutBookingSummariesForNonOwner() {
        User owner = user(1L, "Owner", "owner@example.com");
        Item item = item(10L, "Drill", true, owner);
        ItemResponseDto response = itemResponseDto(10L, "Drill", true, owner.getId());

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(response);
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of());

        ItemResponseDto actualResponse = itemService.getItem(99L, 10L);

        assertThat(actualResponse.getComments()).isEmpty();
        assertThat(actualResponse.getLastBooking()).isNull();
        assertThat(actualResponse.getNextBooking()).isNull();
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(any(), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(any(), any(), any());
    }

    @Test
    void shouldThrowWhenGettingMissingItem() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Item with id 10 not found");
    }

    @Test
    void shouldGetUserItemsWithCommentsAndBookingSummaries() {
        User owner = user(1L, "Owner", "owner@example.com");
        User booker = user(2L, "Booker", "booker@example.com");
        Item firstItem = item(10L, "Drill", true, owner);
        Item secondItem = item(20L, "Saw", true, owner);
        Comment firstComment = comment(100L, "Great", firstItem, booker, LocalDateTime.of(2026, 4, 1, 10, 0));
        Comment secondComment = comment(200L, "Sharp", secondItem, booker, LocalDateTime.of(2026, 4, 2, 10, 0));
        CommentResponseDto firstCommentResponse = commentResponseDto(
                100L,
                "Great",
                "Booker",
                LocalDateTime.of(2026, 4, 1, 10, 0));
        CommentResponseDto secondCommentResponse = commentResponseDto(
                200L,
                "Sharp",
                "Booker",
                LocalDateTime.of(2026, 4, 2, 10, 0));
        Booking lastBooking = booking(
                11L,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(4),
                firstItem,
                booker,
                Booking.BookingStatus.APPROVED);
        Booking nextBooking = booking(
                12L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                secondItem,
                booker,
                Booking.BookingStatus.APPROVED);
        BookingResponseDto lastBookingResponse = bookingResponseDto(
                11L,
                lastBooking.getStart(),
                lastBooking.getEnd(),
                Booking.BookingStatus.APPROVED);
        BookingResponseDto nextBookingResponse = bookingResponseDto(
                12L,
                nextBooking.getStart(),
                nextBooking.getEnd(),
                Booking.BookingStatus.APPROVED);
        ItemResponseDto firstResponse = itemResponseDto(10L, "Drill", true, owner.getId());
        ItemResponseDto secondResponse = itemResponseDto(20L, "Saw", true, owner.getId());

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of(firstItem, secondItem));
        when(commentRepository.findByItemIdInOrderByItemIdAscCreatedDesc(List.of(10L, 20L)))
                .thenReturn(List.of(firstComment, secondComment));
        when(commentMapper.toCommentResponseDto(firstComment)).thenReturn(firstCommentResponse);
        when(commentMapper.toCommentResponseDto(secondComment)).thenReturn(secondCommentResponse);
        when(bookingRepository.findPastApprovedBookingsForItems(eq(List.of(10L, 20L)), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findFutureApprovedBookingsForItems(eq(List.of(10L, 20L)), any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(itemMapper.toItemResponse(firstItem)).thenReturn(firstResponse);
        when(itemMapper.toItemResponse(secondItem)).thenReturn(secondResponse);
        when(bookingMapper.toBookingResponseDto(lastBooking)).thenReturn(lastBookingResponse);
        when(bookingMapper.toBookingResponseDto(nextBooking)).thenReturn(nextBookingResponse);

        List<ItemResponseDto> items = itemService.getUserItems(1L);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getComments()).containsExactly(firstCommentResponse);
        assertThat(items.get(0).getLastBooking()).isEqualTo(lastBookingResponse);
        assertThat(items.get(0).getNextBooking()).isNull();
        assertThat(items.get(1).getComments()).containsExactly(secondCommentResponse);
        assertThat(items.get(1).getLastBooking()).isNull();
        assertThat(items.get(1).getNextBooking()).isEqualTo(nextBookingResponse);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of());

        List<ItemResponseDto> items = itemService.getUserItems(1L);

        assertThat(items).isEmpty();
        verifyNoInteractions(commentMapper, bookingMapper);
    }

    @Test
    void shouldThrowWhenGettingUserItemsForMissingUser() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getUserItems(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id 1 not found");
    }

    @Test
    void shouldSearchItemsWhenTextIsPresent() {
        User owner = user(1L, "Owner", "owner@example.com");
        Item foundItem = item(10L, "Drill", true, owner);
        ItemResponseDto response = itemResponseDto(10L, "Drill", true, owner.getId());

        when(itemRepository.searchItems(eq("drill"), any(Pageable.class))).thenReturn(List.of(foundItem));
        when(itemMapper.toItemResponse(foundItem)).thenReturn(response);

        List<ItemResponseDto> items = itemService.searchItems("drill", 20, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).searchItems(eq("drill"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(items).containsExactly(response);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldReturnEmptyListWhenSearchTextIsBlank(String text) {
        List<ItemResponseDto> items = itemService.searchItems(text, 0, 10);

        assertThat(items).isEmpty();
        verifyNoInteractions(itemRepository, itemMapper);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10, Parameter 'from' must be zero or positive",
            "0, 0, Parameter 'size' must be positive"
    })
    void shouldThrowWhenSearchPaginationIsInvalid(int from, int size, String message) {
        assertThatThrownBy(() -> itemService.searchItems("drill", from, size))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);
    }

    @Test
    void shouldAddCommentWhenUserHasCompletedApprovedBooking() {
        User author = user(1L, "Author", "author@example.com");
        User owner = user(2L, "Owner", "owner@example.com");
        Item item = item(10L, "Drill", true, owner);
        CommentDto commentDto = commentDto("Thanks for the item");
        Comment savedComment = comment(
                100L,
                "Thanks for the item",
                item,
                author,
                LocalDateTime.of(2026, 4, 1, 10, 0));
        CommentResponseDto response = commentResponseDto(
                100L,
                "Thanks for the item",
                "Author",
                LocalDateTime.of(2026, 4, 1, 10, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(1L),
                eq(10L),
                eq(Booking.BookingStatus.APPROVED),
                any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toCommentResponseDto(savedComment)).thenReturn(response);

        CommentResponseDto actualResponse = itemService.addComment(1L, 10L, commentDto);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comment persistedComment = commentCaptor.getValue();

        assertThat(actualResponse).isEqualTo(response);
        assertThat(persistedComment.getText()).isEqualTo("Thanks for the item");
        assertThat(persistedComment.getItem()).isEqualTo(item);
        assertThat(persistedComment.getAuthor()).isEqualTo(author);
        assertThat(persistedComment.getCreated()).isNotNull();
    }

    @Test
    void shouldThrowWhenAddingCommentWithoutCompletedBooking() {
        User author = user(1L, "Author", "author@example.com");
        User owner = user(2L, "Owner", "owner@example.com");
        Item item = item(10L, "Drill", true, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(1L),
                eq(10L),
                eq(Booking.BookingStatus.APPROVED),
                any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(1L, 10L, commentDto("No access")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only users with completed bookings can leave comments");

        verify(commentRepository, never()).save(any(Comment.class));
    }
}
