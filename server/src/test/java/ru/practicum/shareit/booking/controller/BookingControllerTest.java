package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = ShareItApp.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ok() throws Exception {
        BookingCreateDto request = new BookingCreateDto();
        request.setItemId(10L);
        request.setStart(LocalDateTime.now().plusHours(1));
        request.setEnd(LocalDateTime.now().plusHours(2));

        BookingDto response = new BookingDto();
        response.setId(1L);

        Mockito.when(bookingService.create(eq(1L), any(BookingCreateDto.class)))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void approve_ok() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(42L);

        Mockito.when(bookingService.approve(eq(2L), eq(42L), eq(true)))
                .thenReturn(response);

        mvc.perform(patch("/bookings/{bookingId}", 42L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42L));
    }

    @Test
    void getById_ok() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(7L);

        Mockito.when(bookingService.findById(eq(5L), eq(7L)))
                .thenReturn(response);

        mvc.perform(get("/bookings/{bookingId}", 7L)
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L));
    }

    @Test
    void getById_notFound_404() throws Exception {
        Mockito.when(bookingService.findById(eq(5L), eq(999L)))
                .thenThrow(new NotFoundException("Booking not found"));

        mvc.perform(get("/bookings/{bookingId}", 999L)
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isNotFound());
    }
}
