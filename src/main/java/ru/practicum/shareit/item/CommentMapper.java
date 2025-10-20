package ru.practicum.shareit.item;

import org.mapstruct.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toDto(Comment comment);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "text", source = "dto.text"),
            @Mapping(target = "item", source = "item"),
            @Mapping(target = "author", source = "author"),
            @Mapping(target = "created", source = "created")
    })
    Comment toEntity(CommentCreateDto dto, Item item, User author, LocalDateTime created);
}
