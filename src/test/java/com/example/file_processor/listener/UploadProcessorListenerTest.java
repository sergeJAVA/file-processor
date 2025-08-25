package com.example.file_processor.listener;

import com.example.file_processor.constant.FileStatus;
import com.example.file_processor.dto.FileDto;
import com.example.file_processor.service.FileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadProcessorListenerTest {

    @Mock
    private FileValidator fileValidator;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    private UploadProcessorListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        listener = new UploadProcessorListener(fileValidator, objectMapper, kafkaTemplate);
    }

    @Test
    void handle_ValidFile_ShouldSendSuccessStatus() throws Exception {
        FileDto fileDto = FileDto.builder()
                .fileBytes("test".getBytes())
                .fileName("test.xlsx")
                .checksum("123")
                .fileStatus(FileStatus.FILE_ACCEPTED)
                .build();

        String json = objectMapper.writeValueAsString(fileDto);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("upload", 0, 0, "key", json);

        when(fileValidator.validate(any())).thenReturn(true);

        listener.handle(record);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("status"), captor.capture());

        FileDto sent = objectMapper.readValue(captor.getValue(), FileDto.class);
        assertThat(sent.getFileStatus()).isEqualTo(FileStatus.SECOND_VALIDATION_SUCCESS);
    }

    @Test
    void handle_InvalidFile_ShouldSendFailureStatus() throws Exception {
        FileDto fileDto = FileDto.builder()
                .fileBytes("test".getBytes())
                .fileName("test.xlsx")
                .checksum("123")
                .fileStatus(FileStatus.FILE_ACCEPTED)
                .build();

        String json = objectMapper.writeValueAsString(fileDto);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("upload", 0, 0, "key", json);

        when(fileValidator.validate(any())).thenReturn(false);

        listener.handle(record);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("status"), captor.capture());

        FileDto sent = objectMapper.readValue(captor.getValue(), FileDto.class);
        assertThat(sent.getFileStatus()).isEqualTo(FileStatus.SECOND_VALIDATION_FAILURE);
    }
}