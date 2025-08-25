package com.example.file_processor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelFileValidatorTest {


    private ExcelFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ExcelFileValidator();
    }

    @Test
    void validate_validFile_shouldReturnTrue() throws Exception {
        byte[] fileBytes = Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("table.xlsx").toURI())
        );

        boolean result = validator.validate(fileBytes);

        assertThat(result).isTrue();
    }

    @Test
    void validate_invalidFile_shouldReturnFalse() throws Exception {
        byte[] fileBytes = Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("wrongTable.xlsx").toURI())
        );

        boolean result = validator.validate(fileBytes);

        assertThat(result).isFalse();
    }

}