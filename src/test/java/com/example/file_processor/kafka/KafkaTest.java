package com.example.file_processor.kafka;

import com.example.file_processor.constant.FileStatus;
import com.example.file_processor.dto.FileDto;
import com.example.file_processor.testcontainer.Testcontainer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("kafka-test")
public class KafkaTest extends Testcontainer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    private ObjectMapper objectMapper;

    private KafkaConsumer<String, String> statusConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        statusConsumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer("status-processor-id", null);
        statusConsumer.subscribe(List.of("status"));
    }

    @AfterEach
    void tearDown() {
        if (statusConsumer != null) statusConsumer.close();
    }

    @Test
    void testStatusTopic() throws JsonProcessingException {
        FileDto fileDto = FileDto.builder()
                .fileName("testFile")
                .fileBytes("content".getBytes())
                .fileStatus(FileStatus.SECOND_VALIDATION_SUCCESS)
                .checksum("TestChecksum")
                .build();

        String message = objectMapper.writeValueAsString(fileDto);

        kafkaTemplate.send("status", message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records =
                    statusConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                assertThat(record.value()).isEqualTo(message);
            }
            statusConsumer.close();
        });
    }

}
