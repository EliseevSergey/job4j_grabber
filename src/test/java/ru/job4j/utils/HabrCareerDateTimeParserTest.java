package ru.job4j.utils;

import org.junit.jupiter.api.*;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.time.LocalDateTime;

public class HabrCareerDateTimeParserTest {

    @Test
    void whenOkParse() {
        String in = "2023-03-31T17:25:47+03:00";
        LocalDateTime expected =
                LocalDateTime.of(2023, 3, 31,  17,  25, 47, 0);
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        Assertions.assertEquals(expected, parser.parse(in));
    }
}