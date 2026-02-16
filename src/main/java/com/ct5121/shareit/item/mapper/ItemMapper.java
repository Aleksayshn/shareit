package com.ct5121.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.ct5121.shareit.item.dto.ItemRequestDto;
import com.ct5121.shareit.item.dto.ItemResponesDto;
import com.ct5121.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "requestId", ignore = true)
    Item toItem(ItemRequestDto itemRequestDto);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemResponesDto toItemRespones(Item item);
}
