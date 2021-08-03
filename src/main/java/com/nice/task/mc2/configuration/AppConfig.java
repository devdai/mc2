package com.nice.task.mc2.configuration;

import com.nice.task.mc2.Constants;
import com.nice.task.mc2.CustomStompSessionHandler;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class AppConfig {

    //todo break down into separate variables: HOST, PORT, URI etc. And put them in application.properties
    private static final String BOOTSTRAP_ADDRESS = "kafka:29092";
    private static final String WEB_SOCKET_URL = "ws://mc1:8080/mc1/message";

    /**
     * Kafka bootstrap address config
     *
     * @return KafkaAdmin
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESS);
        return new KafkaAdmin(configs);
    }

    /**
     * Simply create a new topic
     *
     * @return new topic
     */
    @Bean
    public NewTopic messageTopic() {
        return new NewTopic(Constants.KAFKA_TOPIC, 1, (short) 1);
    }

    /**
     * Create kafka producer factory. Not to deal too much with serialization, will
     * deserialize to java.lang.String keys and values. Can be improved in future
     *
     * @return Producer factory
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_ADDRESS);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Simple kafka template for sending messages
     *
     * @return KafkaTemplate. Key values serialized to String
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Create custom STOMP Session handler and listen for
     * messages from MC1
     *
     * @return StompSessionHandler
     */
    @Bean
    public StompSessionHandler customStompSessionHandler() {
        WebSocketClient client = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new CustomStompSessionHandler(kafkaTemplate());
        stompClient.connect(WEB_SOCKET_URL, sessionHandler);

        return sessionHandler;
    }
}