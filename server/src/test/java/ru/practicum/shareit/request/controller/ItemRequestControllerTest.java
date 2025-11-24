package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = ShareItApp.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ok() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        request.setDescription("Хочу молоток");

        ItemRequestDto response = new ItemRequestDto();
        response.setId(1L);
        response.setDescription("Хочу молоток");
        response.setCreated(LocalDateTime.now());

        Mockito.when(requestService.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Хочу молоток"));
    }

    @Test
    void getOwn_ok() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Хочу дрель");
        dto.setCreated(LocalDateTime.now());

        Mockito.when(requestService.getOwnRequests(1L))
                .thenReturn(List.of(dto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Хочу дрель"));
    }

    @Test
    void getAll_ok() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(2L);
        dto.setDescription("Нужен молоток");
        dto.setCreated(LocalDateTime.now());

        Mockito.when(requestService.getAllRequests(2L, 0, 10))
                .thenReturn(List.of(dto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].description").value("Нужен молоток"));
    }

    @Test
    void getById_ok() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(5L);
        dto.setDescription("Нужна отвертка");
        dto.setCreated(LocalDateTime.now());

        Mockito.when(requestService.getRequestById(3L, 5L)).thenReturn(dto);

        mvc.perform(get("/requests/{requestId}", 5L)
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.description").value("Нужна отвертка"));
    }

    @Test
    void getById_notFound_404() throws Exception {
        Mockito.when(requestService.getRequestById(1L, 999L))
                .thenThrow(new NotFoundException("Request not found"));

        mvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}
