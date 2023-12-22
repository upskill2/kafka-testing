package dev.tech.dispatch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import static dev.tech.dispatch.service.DispatcherService.ORDER_CREATED_DLT_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
@KafkaListener (
        id = "DLTServiceClient",
        topics = ORDER_CREATED_DLT_TOPIC,
        groupId = "dlt.service.consumer",
        containerFactory = "kafkaListenerContainerFactory")
public class DispatchDeadLetterService {

    @KafkaHandler
    public void listen (@Header (KafkaHeaders.RECEIVED_KEY) String key,
                        @Header (KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                        @Payload String payload) {
        log.info ("DLT event received: {} from topic: {}", payload, ORDER_CREATED_DLT_TOPIC);
    }
}
