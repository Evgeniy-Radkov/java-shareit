package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize_createdAndItems_ok() throws Exception {
        ItemRequestShortDto item = new ItemRequestShortDto();
        item.setId(5L);
        item.setName("Дрель");
        item.setOwnerId(1L);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);
        dto.setDescription("Нужна дрель");
        dto.setCreated(LocalDateTime.of(2025, 11, 4, 12, 30));
        dto.setItems(Collections.singletonList(item));

        JsonContent<ItemRequestDto> content = json.write(dto);

        assertThat(content).hasJsonPathNumberValue("$.id");
        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(content).hasJsonPathArrayValue("$.items");
        assertThat(content).extractingJsonPathStringValue("$.created")
                .startsWith("2025-11-04T12:30");
    }
}
