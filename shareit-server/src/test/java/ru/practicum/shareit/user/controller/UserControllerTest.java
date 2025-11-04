package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Test
    void create_ok() throws Exception {
        UserDto request = new UserDto(null, "Саша", "sasha@mail.com");
        UserDto response = new UserDto(1L, "Саша", "sasha@mail.com");

        Mockito.when(userService.create(Mockito.any(UserDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("sasha@mail.com"));
    }

    @Test
    void update_conflictEmail_409() throws Exception {
        Mockito.doThrow(new ConflictException("Email already in use"))
                .when(userService).update(Mockito.eq(2L), Mockito.any(UserDto.class));

        mvc.perform(MockMvcRequestBuilders.patch("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dup@mail.com\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }


    @Test
    void getById_ok() throws Exception {
        UserDto response = new UserDto(1L, "Саша", "sasha@mail.com");
        Mockito.when(userService.findById(1L)).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Саша"));
    }

    @Test
    void getById_notFound_404() throws Exception {
        Mockito.when(userService.findById(99L))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(MockMvcRequestBuilders.get("/users/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void delete_ok() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        Mockito.verify(userService, Mockito.times(1)).delete(1L);
    }
}
