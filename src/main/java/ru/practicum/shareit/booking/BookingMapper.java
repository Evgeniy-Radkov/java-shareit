package ru.practicum.shareit.booking;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mappings({
            @Mapping(target = "booker.id", source = "booker.id"),
            @Mapping(target = "item.id",   source = "item.id"),
            @Mapping(target = "item.name", source = "item.name")
    })
    BookingDto toDto(Booking booking);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "item", source = "item"),
            @Mapping(target = "booker", source = "booker"),
            @Mapping(target = "status", constant = "WAITING")
    })
    Booking toEntity(BookingCreateDto dto, Item item, User booker);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", source = "status")
    void updateStatus(@MappingTarget Booking booking, BookingStatus status);
}
