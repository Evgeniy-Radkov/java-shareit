package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

@Mapper(componentModel = "spring")
public interface ItemRequestShortMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "ownerId", source = "owner.id")
    ItemRequestShortDto toShortDto(Item item);
}
