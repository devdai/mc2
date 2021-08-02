package com.nice.task.mc2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nice.task.mc2.dto.MessageDTO;
import lombok.SneakyThrows;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class CustomStompSessionHandler extends StompSessionHandlerAdapter {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public CustomStompSessionHandler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Subscribe to destination topic and listen for messages from MC1
     *
     * @param session current session
     * @param connectedHeaders headers
     */
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/MC2", new StompFrameHandler() {
            /**
             * Set payload type to our MessageDTO.class regardless of headers
             *
             * @param headers headers
             * @return MessageDTO.class
             */
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageDTO.class;
            }

            /**
             * Handle message from MC1, serialize to String and send into kafka topic
             *
             * @param headers headers
             * @param payload message payload
             */
            @SneakyThrows
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                MessageDTO message = (MessageDTO) payload;
                message.setMC2_timestamp(Date.from(Instant.now()));
                kafkaTemplate.send(Constants.KAFKA_TOPIC, new ObjectMapper().writeValueAsString(message));
            }
        });
    }
}
