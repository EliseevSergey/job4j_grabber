package ru.job4j.utils;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;

class HabrCareerDateTimeParserTest {

    @Test
    void whenOkparse() {
        String in = "2023-03-31T17:25:47+03:00";
        LocalDateTime expected =
                LocalDateTime.of(2023, 3, 31,  17,  25, 47, 0);
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        Assertions.assertEquals(expected, parser.parse(in));
    }
}