package com.example.phamnav.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.name:pharmacy-data}")
    private String topicName;

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replication-factor:3}")
    private short replicationFactor;

    @Bean
    public NewTopic pharmacyTopic() {
        NewTopic topic = TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicationFactor)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
        log.info("Created Kafka topic: {}", topic);
        return topic;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @PostConstruct
    public void initializeKafkaTopic() {
        AdminClient adminClient = AdminClient.create(
                Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        );

        try {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            if (!existingTopics.contains("pharmacy-data")) {
                adminClient.createTopics(Collections.singleton(pharmacyTopic())).all().get();
                log.info("pharmacy-data 토픽이 성공적으로 생성되었습니다.");
            } else {
                log.info("pharmacy-data 토픽이 이미 존재합니다.");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kafka 토픽 초기화 중 오류 발생: ", e);
        } finally {
            adminClient.close();
        }
    }
}