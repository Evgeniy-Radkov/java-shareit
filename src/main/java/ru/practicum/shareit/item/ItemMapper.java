package ru.practicum.shareit.item;

import org.mapstruct.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "requestId", source = "requestId")
    Item toItem(ItemDto dto);

    @Mappings({
            @Mapping(target = "ownerId", source = "owner.id"),
            @Mapping(target = "requestId", source = "requestId")
    })
    ItemDto toItemDto(Item item);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "requestId", source = "requestId")
    void updateItemFromDto(ItemDto dto, @MappingTarget Item target);
}
