package com.ct5121.shareit.item.service;

import com.ct5121.shareit.booking.mapper.BookingMapper;
import com.ct5121.shareit.booking.model.Booking;
import com.ct5121.shareit.booking.repository.BookingRepository;
import com.ct5121.shareit.exception.BadRequestException;
import com.ct5121.shareit.exception.NotFoundException;
import com.ct5121.shareit.item.dto.CommentDto;
import com.ct5121.shareit.item.dto.CommentResponseDto;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponseDto;
import com.ct5121.shareit.item.mapper.CommentMapper;
import com.ct5121.shareit.item.mapper.ItemMapper;
import com.ct5121.shareit.item.model.Comment;
import com.ct5121.shareit.item.model.Item;
import com.ct5121.shareit.item.repository.CommentRepository;
import com.ct5121.shareit.item.repository.ItemRepository;
import com.ct5121.shareit.user.model.User;
import com.ct5121.shareit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemResponseDto addItem(Long userId, ItemRequestDto itemRequestDto) {
        User owner = getExistingUser(userId);
        Item item = itemMapper.toItem(itemRequestDto);
        item.setOwner(owner);
        return itemMapper.toItemResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto) {
        Item item = getExistingItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User is not the owner of this item");
        }

        if (itemRequestDto.getName() != null) {
            item.setName(itemRequestDto.getName());
        }
        if (itemRequestDto.getDescription() != null) {
            item.setDescription(itemRequestDto.getDescription());
        }
        if (itemRequestDto.getAvailable() != null) {
            item.setAvailable(itemRequestDto.getAvailable());
        }

        return itemMapper.toItemResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getItem(Long userId, Long itemId) {
        Item item = getExistingItem(itemId);
        ItemResponseDto dto = itemMapper.toItemResponse(item);

        dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(commentMapper::toCommentResponseDto)
                .toList());

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            itemId,
                            Booking.BookingStatus.APPROVED,
                            now)
                    .ifPresent(booking -> dto.setLastBooking(bookingMapper.toBookingResponseDto(booking)));

            bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            itemId,
                            Booking.BookingStatus.APPROVED,
                            now)
                    .ifPresent(booking -> dto.setNextBooking(bookingMapper.toBookingResponseDto(booking)));
        }

        return dto;
    }

    @Override
    public List<ItemResponseDto> getUserItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();

        // Batch-load comments and bookings for all items to prevent N+1 query bursts.
        Map<Long, List<CommentResponseDto>> commentsByItemId = groupCommentsByItemId(itemIds);
        Map<Long, Booking> lastBookingByItemId = takeFirstBookingPerItem(
                bookingRepository.findPastApprovedBookingsForItems(itemIds, now));
        Map<Long, Booking> nextBookingByItemId = takeFirstBookingPerItem(
                bookingRepository.findFutureApprovedBookingsForItems(itemIds, now));

        return items.stream()
                .map(item -> {
                    ItemResponseDto dto = itemMapper.toItemResponse(item);
                    dto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()));

                    Booking lastBooking = lastBookingByItemId.get(item.getId());
                    if (lastBooking != null) {
                        dto.setLastBooking(bookingMapper.toBookingResponseDto(lastBooking));
                    }

                    Booking nextBooking = nextBookingByItemId.get(item.getId());
                    if (nextBooking != null) {
                        dto.setNextBooking(bookingMapper.toBookingResponseDto(nextBooking));
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItems(String text, int from, int size) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        Pageable pageable = buildOffsetPageRequest(from, size);
        return itemRepository.searchItems(text, pageable).stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getExistingUser(userId);
        Item item = getExistingItem(itemId);

        boolean hasApprovedPastBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId,
                itemId,
                Booking.BookingStatus.APPROVED,
                LocalDateTime.now());

        if (!hasApprovedPastBooking) {
            throw new BadRequestException("Only users with completed bookings can leave comments");
        }

        Comment comment = new Comment(
                null,
                commentDto.getText(),
                item,
                author,
                LocalDateTime.now());
        return commentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    private User getExistingUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private Item getExistingItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));
    }

    private Pageable buildOffsetPageRequest(int from, int size) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be zero or positive");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        return PageRequest.of(from / size, size);
    }

    private Map<Long, List<CommentResponseDto>> groupCommentsByItemId(Collection<Long> itemIds) {
        Map<Long, List<CommentResponseDto>> result = new HashMap<>();
        for (Comment comment : commentRepository.findByItemIdInOrderByItemIdAscCreatedDesc(itemIds)) {
            Long itemId = comment.getItem().getId();
            result.computeIfAbsent(itemId, ignored -> new java.util.ArrayList<>())
                    .add(commentMapper.toCommentResponseDto(comment));
        }
        return result;
    }

    private Map<Long, Booking> takeFirstBookingPerItem(List<Booking> bookings) {
        Map<Long, Booking> result = new HashMap<>();
        for (Booking booking : bookings) {
            result.putIfAbsent(booking.getItem().getId(), booking);
        }
        return result;
    }
}
