package com.example.file_processor.service;

import java.io.IOException;

public interface FileValidator {
    boolean validate(byte[] fileBytes) throws IOException;

}
