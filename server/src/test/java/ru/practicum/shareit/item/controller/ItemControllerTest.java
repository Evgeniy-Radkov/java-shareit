package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = ShareItApp.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_created_201() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Дрель");
        request.setDescription("С ударом");
        request.setAvailable(Boolean.TRUE);
        request.setRequestId(10L);

        ItemDto response = new ItemDto();
        response.setId(1L);
        response.setName("Дрель");
        response.setDescription("С ударом");
        response.setAvailable(Boolean.TRUE);
        response.setRequestId(10L);

        Mockito.when(itemService.create(any(ItemDto.class), eq(1L))).thenReturn(response);

        mvc.perform(post("/items")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void update_ok_200() throws Exception {
        ItemDto patch = new ItemDto();
        patch.setDescription("Апгрейд");

        ItemDto response = new ItemDto();
        response.setId(2L);
        response.setName("Отвертка");
        response.setDescription("Апгрейд");
        response.setAvailable(Boolean.TRUE);

        Mockito.when(itemService.update(any(ItemDto.class), eq(2L), eq(1L))).thenReturn(response);

        mvc.perform(patch("/items/{itemId}", 2L)
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.description").value("Апгрейд"));
    }

    @Test
    void getById_ok_200() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(3L);
        response.setName("Пила");
        response.setDescription("По дереву");
        response.setAvailable(Boolean.TRUE);

        Mockito.when(itemService.findById(eq(3L), eq(5L))).thenReturn(response);

        mvc.perform(get("/items/{itemId}", 3L)
                        .header(HDR, 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Пила"));
    }

    @Test
    void getById_notFound_404() throws Exception {
        Mockito.when(itemService.findById(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Item not found"));

        mvc.perform(get("/items/{itemId}", 999L)
                        .header(HDR, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnerItems_ok_200_withPagination() throws Exception {
        ItemDto i1 = new ItemDto();
        i1.setId(1L);
        i1.setName("A");
        i1.setDescription("d1");
        i1.setAvailable(Boolean.TRUE);

        ItemDto i2 = new ItemDto();
        i2.setId(2L);
        i2.setName("B");
        i2.setDescription("d2");
        i2.setAvailable(Boolean.TRUE);

        Mockito.when(itemService.findAllByOwner(eq(7L), eq(0), eq(2)))
                .thenReturn(List.of(i1, i2));

        mvc.perform(get("/items")
                        .header(HDR, 7L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void search_ok_200_nonEmptyText() throws Exception {
        ItemDto it = new ItemDto();
        it.setId(4L);
        it.setName("Дрель про");
        it.setDescription("Мощная");
        it.setAvailable(Boolean.TRUE);

        Mockito.when(itemService.search(eq("дрель")))
                .thenReturn(List.of(it));

        mvc.perform(get("/items/search")
                        .header(HDR, 1L)
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель про"));
    }

    @Test
    void search_emptyText_returnsEmptyList_200() throws Exception {
        Mockito.when(itemService.search(eq(""))).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .header(HDR, 1L)
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void addComment_ok_200() throws Exception {
        CommentCreateDto req = new CommentCreateDto();
        req.setText("Отличная вещь!");

        CommentDto resp = new CommentDto();
        resp.setId(11L);
        resp.setText("Отличная вещь!");
        resp.setAuthorName("Саша");

        Mockito.when(itemService.addComment(eq(2L), eq(3L), any(CommentCreateDto.class)))
                .thenReturn(resp);

        mvc.perform(post("/items/{itemId}/comment", 3L)
                        .header(HDR, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.authorName").value("Саша"));
    }

    @Test
    void addComment_validationFail_400() throws Exception {
        CommentCreateDto req = new CommentCreateDto();
        req.setText("bad");

        Mockito.when(itemService.addComment(eq(2L), eq(3L), any(CommentCreateDto.class)))
                .thenThrow(new ValidationException("User has no approved bookings"));

        mvc.perform(post("/items/{itemId}/comment", 3L)
                        .header(HDR, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
