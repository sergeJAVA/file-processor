package com.example.file_processor.listener;

import com.example.file_processor.constant.FileStatus;
import com.example.file_processor.dto.FileDto;
import com.example.file_processor.service.FileValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadProcessorListener {

    private final FileValidator fileValidator;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "upload", groupId = "processor-id")
    public void handle(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        FileDto event = objectMapper.readValue(consumerRecord.value(), FileDto.class);
        fileValidation(event);
    }

    @SneakyThrows
    private void fileValidation(FileDto event) {
        boolean isValid = fileValidator.validate(event.getFileBytes());
        if (isValid) {
            kafkaTemplate.send("status", objectMapper.writeValueAsString(changeStatusToSuccess(event)));
        } else {
            kafkaTemplate.send("status", objectMapper.writeValueAsString(changeStatusToFailure(event)));
        }
    }

    private FileDto changeStatusToSuccess(FileDto event) {
        event.setFileStatus(FileStatus.SECOND_VALIDATION_SUCCESS);
        return event;
    }

    private FileDto changeStatusToFailure(FileDto event) {
        event.setFileStatus(FileStatus.SECOND_VALIDATION_FAILURE);
        return event;
    }

}
